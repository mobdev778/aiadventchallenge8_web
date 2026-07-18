package com.github.mobdev778.aiadventchallenge.web.controller.home

import com.github.mobdev778.aiadventchallenge.domain.chat.model.MessageType
import com.github.mobdev778.aiadventchallenge.domain.chat.service.ChatService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.view.RedirectView
import java.util.UUID

@Controller
class HomeController(
    private val chatService: ChatService,
) {

    @GetMapping("/")
    fun index(): String {
        return "index"
    }

    @PostMapping("/chats")
    suspend fun createChat(
        @RequestParam("question") question: String,
    ): RedirectView {
        val chat = chatService.createChatWithFirstMessage(question)
        return RedirectView("/chats/${chat.id}")
    }

    @GetMapping("/chats/{chatId}")
    fun chat(
        @PathVariable chatId: String,
        model: Model,
    ): String {
        val chatView = chatService.getChatView(chatId)
        val lastMessage = chatView.messages.lastOrNull()
        model.addAttribute("chat", chatView.chat)
        model.addAttribute("messages", chatView.messages)
        model.addAttribute("showReplyForm", lastMessage?.type == MessageType.Bot)
        model.addAttribute("lastBotMessageId", lastMessage?.id)
        return "chat"
    }

    @PostMapping("/chats/{chatId}/messages")
    suspend fun postUserMessage(
        @PathVariable chatId: UUID,
        @RequestParam("lastMessageId") lastMessageId: UUID,
        @RequestParam("userMessage") userMessage: String,
    ): RedirectView {
        chatService.postUserMessage(chatId, lastMessageId, userMessage)
        return RedirectView("/chats/$chatId")
    }

    @GetMapping("/chats/{chatId}/events")
    fun subscribeToChatUpdates(
        @PathVariable chatId: UUID,
    ): SseEmitter {
        return chatService.subscribeToChatUpdates(chatId)
    }
}