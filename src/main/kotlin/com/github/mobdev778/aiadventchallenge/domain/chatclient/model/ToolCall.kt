package com.github.mobdev778.aiadventchallenge.domain.chatclient.model

data class ToolCall(
    val id: String,
    // В OpenAI всегда "function"
    val type: String,
    val function: FunctionCall,
)
