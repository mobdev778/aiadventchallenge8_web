package com.github.mobdev778.aiadventchallenge.domain.chatclient.model

import io.modelcontextprotocol.kotlin.sdk.types.Tool

data class ChatRequest(
    val model: String,
    val reasoningEffort: ReasoningEffort? = null,
    val messages: List<Message>,
    val maxTokens: Int? = null,
    val temperature: Double = 0.7,
    val tools: List<Tool> = emptyList(),
)
