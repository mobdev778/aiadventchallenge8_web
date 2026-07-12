package com.github.mobdev778.aiadventchallenge.domain.chatclient.model

data class Message(
    val role: Role,
    val content: String?,
    val toolCalls: List<ToolCall>? = null,
    val toolCallId: String? = null,
    val name: String? = null
)
