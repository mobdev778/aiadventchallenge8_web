package com.github.mobdev778.aiadventchallenge.domain.chat.service

import com.github.mobdev778.aiadventchallenge.domain.chat.model.Chat
import com.github.mobdev778.aiadventchallenge.domain.chat.model.ChatMessage

data class ChatView(
    val chat: Chat,
    val messages: List<ChatMessage>,
)