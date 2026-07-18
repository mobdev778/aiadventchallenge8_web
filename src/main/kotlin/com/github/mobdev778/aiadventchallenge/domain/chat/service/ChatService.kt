package com.github.mobdev778.aiadventchallenge.domain.chat.service

import com.github.mobdev778.aiadventchallenge.domain.agent.AgentOrchestrator
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.chat.model.Chat
import com.github.mobdev778.aiadventchallenge.domain.chat.model.ChatMessage
import com.github.mobdev778.aiadventchallenge.domain.chat.model.MessageType
import com.github.mobdev778.aiadventchallenge.domain.chat.persistence.ChatEntity
import com.github.mobdev778.aiadventchallenge.domain.chat.persistence.ChatMessageEntity
import com.github.mobdev778.aiadventchallenge.domain.chat.persistence.ChatMessageRepository
import com.github.mobdev778.aiadventchallenge.domain.chat.persistence.ChatRepository
import jakarta.transaction.Transactional
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val agentOrchestrator: AgentOrchestrator,
    private val chatUpdateNotifier: ChatUpdateNotifier,
    @Value("\${ai.chat.base-model}")
    private val baseModel: String,
) {

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Transactional
    suspend fun createChatWithFirstMessage(question: String): Chat {
        val normalizedQuestion = question.trim()
        require(normalizedQuestion.isNotEmpty()) { "Question must not be blank" }

        val now = Instant.now().toEpochMilli()
        val chat = Chat(
            id = UUID.randomUUID(),
            name = buildChatName(normalizedQuestion),
            time = now,
            parentId = null,
        )

        chatRepository.save(chat.toEntity())
        postUserMessage(chat.id, null, normalizedQuestion)
        return chat
    }

    @Transactional
    suspend fun postUserMessage(
        chatId: UUID,
        lastMessageId: UUID?,
        userMessage: String
    ) {
        backgroundScope.launch {
            val normalizedMessage = userMessage.trim()
            require(normalizedMessage.isNotEmpty()) { "User message must not be blank" }

            val chatMessage = ChatMessage(
                id = UUID.randomUUID(),
                chatId = chatId,
                parentId = lastMessageId,
                time = System.currentTimeMillis(),
                text = normalizedMessage,
                type = MessageType.User,
                tokens = 0,
                toolCallId = null,
                name = null,
            )
            chatMessageRepository.save(chatMessage.toEntity())
            chatUpdateNotifier.notifyAssistantThinking(chatId)
            sendRequestAsync(chatMessage)
        }
    }

    @Async
    suspend fun sendRequestAsync(userMessage: ChatMessage) {
        val request = AgentRequest(
            chatId = userMessage.chatId,
            parentMessageId = userMessage.parentId,
            time = userMessage.time,
            query = userMessage.text,
        )
        agentOrchestrator.asyncRequest(request)
    }

    @EventListener(ApplicationReadyEvent::class)
    fun startListening() {
        agentOrchestrator.startAgents()
        backgroundScope.launch {
            agentOrchestrator.responses.collect { response ->
                saveAssistantMessage(
                    chatId = response.request.chatId,
                    parentId = response.request.parentMessageId,
                    assistantMessage = response.message,
                    responseTokens = response.responseTokens
                )
            }
        }
    }

    @EventListener(ContextClosedEvent::class)
    fun stopListening() {
        backgroundScope.cancel()
        agentOrchestrator.stopAgents()
    }

    @Transactional
    fun saveAssistantMessage(chatId: UUID, parentId: UUID?, assistantMessage: String, responseTokens: Int) {
        val chatMessage = ChatMessage(
            id = UUID.randomUUID(),
            chatId = chatId,
            parentId = parentId,
            time = System.currentTimeMillis(),
            text = assistantMessage,
            type = MessageType.Bot,
            tokens = responseTokens,
            toolCallId = null,
            name = null,
        )
        chatMessageRepository.save(chatMessage.toEntity())
        chatUpdateNotifier.notifyChatUpdated(chatId)
    }

    @Transactional
    fun getChatView(chatId: String): ChatView {
        val uuid = runCatching { UUID.fromString(chatId) }
            .getOrElse { throw ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found") }

        val chat = chatRepository.findById(uuid)
            .map { it.toModel() }
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Chat not found") }

        val messages = chatMessageRepository.findAllByChatIdOrderByTimeAsc(uuid)
            .map { it.toModel() }

        return ChatView(chat = chat, messages = messages)
    }

    fun subscribeToChatUpdates(chatId: UUID) = chatUpdateNotifier.subscribe(chatId)

    private fun buildChatName(question: String): String {
        return question.take(60).ifBlank { "Новый чат" }
    }
}

fun Chat.toEntity(): ChatEntity = ChatEntity(
    id = id,
    name = name,
    time = time,
    parentId = parentId,
)

fun ChatEntity.toModel(): Chat = Chat(
    id = id,
    name = name,
    time = time,
    parentId = parentId,
)

fun ChatMessage.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    chatId = chatId,
    parentId = parentId,
    time = time,
    text = text,
    type = type,
    tokens = tokens,
    toolCallId = toolCallId,
    name = name,
)

fun ChatMessageEntity.toModel(): ChatMessage = ChatMessage(
    id = id,
    chatId = chatId,
    parentId = parentId,
    time = time,
    text = text,
    type = type,
    tokens = tokens,
    toolCallId = toolCallId,
    name = name,
)
