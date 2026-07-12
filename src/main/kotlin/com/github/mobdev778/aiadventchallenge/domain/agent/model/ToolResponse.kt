package com.github.mobdev778.aiadventchallenge.domain.agent.model

data class ToolResponse(
    val toolCallId: String,
    val name: String,
    val content: String,
)