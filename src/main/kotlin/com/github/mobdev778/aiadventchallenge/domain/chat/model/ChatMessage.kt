package com.github.mobdev778.aiadventchallenge.domain.chat.model

import java.util.UUID

data class ChatMessage(
    val id: UUID,
    val chatId: UUID,
    val parentId: UUID?,
    val time: Long,
    val text: String,
    val type: MessageType,
    val tokens: Int,
    val toolCallId: String? = null,
    val name: String? = null,
)
