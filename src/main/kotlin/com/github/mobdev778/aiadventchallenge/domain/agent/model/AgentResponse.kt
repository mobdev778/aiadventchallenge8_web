package com.github.mobdev778.aiadventchallenge.domain.agent.model

data class AgentResponse(
    val agent: String,
    val request: AgentRequest,
    val message: String,
    val toolMessages: List<ToolResponse>,
    val requestTokens: Int,
    val responseTokens: Int,
)