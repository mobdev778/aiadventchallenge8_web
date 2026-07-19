package com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm

import com.github.mobdev778.aiadventchallenge.data.chatclient.repository.ChatClientRepository
import com.github.mobdev778.aiadventchallenge.domain.agent.AgentOrchestrator
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.BaseAgent
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context.CrmContextRegistry
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context.CrmTicket
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context.CrmUser
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentContext
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentResponse
import com.github.mobdev778.aiadventchallenge.domain.mcp.McpServerInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

class CrmChatAgent(
    id: String,
    chatClientRepository: ChatClientRepository,
    mcpServerInteractor: McpServerInteractor,
    coroutineScope: CoroutineScope,
    baseModel: String,
) : BaseAgent(
    id, chatClientRepository, mcpServerInteractor, coroutineScope, baseModel,
) {

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    override suspend fun handle(
        agentOrchestrator: AgentOrchestrator,
        context: AgentContext,
        request: AgentRequest
    ): AgentResponse? {
        println("!!! CrmChatAgent. query: ${request.query}")

        val crmContext = CrmContextRegistry.get(request.chatId)!!

        val response = sendMessage(
            context,
            getSystemPrompt(crmContext.user, crmContext.ticket),
            request,
        )

        val ticket: CrmTicket? = try {
            json.decodeFromString<CrmTicket>(response.message)
        } catch (e: Exception) {
            null
        }

        return if (ticket != null) {
            CrmContextRegistry.put(request.chatId, crmContext.copy(ticket = ticket))
            agentOrchestrator.asyncRequest(
                request.copy(query = "Изучи мою заявку и предложи решение")
            )
            null
        } else {
            response
        }
    }

    private fun getSystemPrompt(user: CrmUser, ticket: CrmTicket?) = """
        Ты — AI-ассистент поддержки. Отвечай вежливо, используя только предоставленные данные.
        Если знаешь имя пользователя - всегда обращайся к нему по имени.
        Для поиска тикета пользователя можешь использовать MCP-инструмент "search_ticket_by_user" -
        просто верни JSON-объект тикета в ответе и я обновлю контекст чата. 

        [ДАННЫЕ ТИКЕТА ИЗ CRM И MCP]:
        - ID тикета: ${ticket?.ticketId}
        - Клиент: ${user.name} (email: ${user.email})
        - Платформа: ${user.activeDevice.platform}, версия приложения: ${user.activeDevice.appVersion}
        - Статус: ${ticket?.status}

        [Logs]:
        ${ticket?.systemLogs}

        [ИСТОРИЯ ДИАЛОГА]:
        ${ticket?.messages}
    """.trimIndent()
}