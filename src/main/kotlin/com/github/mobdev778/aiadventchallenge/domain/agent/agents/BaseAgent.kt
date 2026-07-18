package com.github.mobdev778.aiadventchallenge.domain.agent.agents

import com.github.mobdev778.aiadventchallenge.data.chatclient.repository.ChatClientRepository
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentContext
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentResponse
import com.github.mobdev778.aiadventchallenge.domain.agent.model.ToolResponse
import com.github.mobdev778.aiadventchallenge.domain.chat.model.ChatMessage
import com.github.mobdev778.aiadventchallenge.domain.chat.model.MessageType
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.ChatRequest
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.Message
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.Role
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.ToolCall
import com.github.mobdev778.aiadventchallenge.domain.mcp.McpServerInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.UUID

abstract class BaseAgent(
    id: String,
    private val chatClientRepository: ChatClientRepository,
    private val mcpServerInteractor: McpServerInteractor,
    private val coroutineScope: CoroutineScope,
    private val baseModel: String,
) : Agent(id) {

    override suspend fun handle(
        context: AgentContext,
        request: AgentRequest
    ): AgentResponse? {
        val systemPrompt = SystemPromptBuilder()
            .chatId(request.chatId)
            .profile(context.profile)     // Долговременная память
            .query(request.query)
            .build()

        return sendMessage(
            context = context,
            systemPrompt = systemPrompt,
            request = request,
        )
    }

    suspend fun sendMessage(
        context: AgentContext,
        systemPrompt: String,
        request: AgentRequest,
    ): AgentResponse {
        println("!!! BaseAgent.sendMessage. request: ${request.query}")

        val userMessage = buildUserMessage(request)

        println("!!! BaseAgent.sendMessage. [1]")

        val currentMessages = try {
            buildCurrentMessages(context, systemPrompt, userMessage)
        } catch (e: Exception) {
            ArrayList()
        }

        println("!!! BaseAgent.sendMessage. [2]")

        val (finalAnswer, totalPromptTokens, totalCompletionTokens) =
            executeAgentLoop(context, currentMessages)

        println("!!! BaseAgent.sendMessage. [3]")

        val responseMessage = ChatMessage(
            id = UUID.randomUUID(),
            chatId = userMessage.chatId,
            parentId = userMessage.id,
            time = System.currentTimeMillis(),
            text = finalAnswer,
            type = MessageType.Bot,
            tokens = totalCompletionTokens,
        )

        println("!!! BaseAgent.sendMessage. [4]")

        return AgentResponse(
            agent = this.toString(),
            request = request,
            toolMessages = emptyList(),
            message = responseMessage.text,
            requestTokens = totalPromptTokens,
            responseTokens = responseMessage.tokens,
        )
    }

    private fun buildUserMessage(request: AgentRequest): ChatMessage {
        return ChatMessage(
            id = UUID.randomUUID(),
            chatId = request.chatId,
            parentId = request.parentMessageId,
            time = System.currentTimeMillis(),
            text = request.query,
            type = MessageType.User,
            tokens = 0,
        )
    }

    private fun buildCurrentMessages(
        context: AgentContext,
        systemPrompt: String,
        userMessage: ChatMessage,
    ): ArrayList<Message> {
        val currentMessages = ArrayList<Message>()
        currentMessages.add(Message(Role.System, systemPrompt))

        context.messages.forEach { message ->
            val mapped = when (message.type) {
                MessageType.User -> Message(Role.User, content = message.text)
                MessageType.Bot -> Message(Role.Assistant, content = message.text)
                MessageType.Tool -> Message(
                    Role.Tool,
                    content = message.text,
                    toolCallId = message.toolCallId,
                    name = message.name
                )
            }
            currentMessages.add(mapped)
        }

        currentMessages.add(Message(Role.User, content = userMessage.text))
        return currentMessages
    }

    private suspend fun executeAgentLoop(
        context: AgentContext,
        currentMessages: ArrayList<Message>,
    ): Triple<String, Int, Int> {
        println("!!! executeAgentLoop()")

        var totalPromptTokens = 0
        var totalCompletionTokens = 0
        var finalAnswer = "- no response -"
        var maxIterations = MAX_AGENT_ITERATIONS
        var shouldContinue = true

        try {
            while (shouldContinue && maxIterations > 0) {
                maxIterations--

                val request = ChatRequest(
                    model = baseModel,
                    messages = currentMessages,
                    tools = context.tools,
                    temperature = 0.4, // снижаем температуру, чтобы улучшить качество поиска
                )
                println("!!! request: $request")

                val response = chatClientRepository.sendRequest(request)
                println("!!! response: ${response.choices.firstOrNull()?.message?.content}")

                totalPromptTokens += response.usage?.promptTokens ?: 0
                totalCompletionTokens += response.usage?.completionTokens ?: 0

                val assistantMessage = response.choices.firstOrNull()?.message
                val toolCalls = assistantMessage?.toolCalls

                currentMessages.add(
                    Message(
                        role = Role.Assistant,
                        content = assistantMessage?.content,
                        toolCalls = toolCalls
                    )
                )

                if (!toolCalls.isNullOrEmpty()) {
                    executeToolCalls(currentMessages, toolCalls)
                } else {
                    finalAnswer = assistantMessage?.content?.takeIf { it.isNotBlank() }
                        ?: "- no response -"
                    shouldContinue = false
                }
            }
        } catch (e: Exception) {
            finalAnswer = "Ошибка: ${e.message ?: e::class.java.simpleName}"
        }

        return Triple(finalAnswer, totalPromptTokens, totalCompletionTokens)
    }

    private suspend fun executeToolCalls(
        currentMessages: ArrayList<Message>,
        toolCalls: List<ToolCall>,
    ) {
        println("!!! Вызов инструментов: $toolCalls")

        val jobs = toolCalls.map { toolCall ->
            coroutineScope.async {
                sendMcpMessage(toolCall.copy(type = "function"))
            }
        }

        val results = jobs.awaitAll()

        results.filterNotNull().forEach { toolResponse ->
            currentMessages.add(
                Message(
                    role = Role.Tool,
                    content = toolResponse.content,
                    toolCallId = toolResponse.toolCallId,
                    name = toolResponse.name
                )
            )
        }
    }

    suspend fun sendMcpMessage(toolCall: ToolCall): ToolResponse? {
        return mcpServerInteractor.sendRequest(toolCall)
    }

    companion object {
        private const val MAX_AGENT_ITERATIONS = 5
    }
}
