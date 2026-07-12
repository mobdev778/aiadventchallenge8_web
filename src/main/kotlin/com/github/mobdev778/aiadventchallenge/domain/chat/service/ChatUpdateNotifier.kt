package com.github.mobdev778.aiadventchallenge.domain.chat.service

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class ChatUpdateNotifier {

    private val emittersByChatId = ConcurrentHashMap<UUID, MutableSet<SseEmitter>>()

    fun subscribe(chatId: UUID): SseEmitter {
        val emitter = SseEmitter(0L)
        val emitters = emittersByChatId.computeIfAbsent(chatId) { ConcurrentHashMap.newKeySet() }
        emitters += emitter

        emitter.onCompletion { removeEmitter(chatId, emitter) }
        emitter.onTimeout { removeEmitter(chatId, emitter) }
        emitter.onError { removeEmitter(chatId, emitter) }

        sendEvent(chatId, emitter, "connected")
        return emitter
    }

    @Async
    fun notifyChatUpdated(chatId: UUID) {
        notify(chatId, "chat-updated")
    }

    @Async
    fun notifyAssistantThinking(chatId: UUID) {
        notify(chatId, "assistant-thinking")
    }

    @Async
    fun notifyAssistantReady(chatId: UUID) {
        notify(chatId, "assistant-ready")
    }

    private fun notify(chatId: UUID, eventName: String) {
        val emitters = emittersByChatId[chatId]?.toList().orEmpty()
        emitters.forEach { emitter ->
            sendEvent(chatId, emitter, eventName)
        }
    }

    private fun sendEvent(chatId: UUID, emitter: SseEmitter, eventName: String) {
        try {
            emitter.send(
                SseEmitter.event()
                    .name(eventName)
                    .data(ChatUpdatedEvent(chatId = chatId.toString()))
            )
        } catch (_: IOException) {
            removeEmitter(chatId, emitter)
        } catch (_: IllegalStateException) {
            removeEmitter(chatId, emitter)
        }
    }

    private fun removeEmitter(chatId: UUID, emitter: SseEmitter) {
        emittersByChatId.computeIfPresent(chatId) { _, emitters ->
            emitters.remove(emitter)
            emitters.takeIf { it.isNotEmpty() }
        }
    }
}

data class ChatUpdatedEvent(
    val chatId: String,
)
