package com.github.mobdev778.aiadventchallenge.domain.agent.model

import com.github.mobdev778.aiadventchallenge.domain.chat.model.ChatMessage
import com.github.mobdev778.aiadventchallenge.domain.profile.model.Profile
import io.modelcontextprotocol.kotlin.sdk.types.Tool

data class AgentContext(
    val profile: Profile,
    val messages: List<ChatMessage>,
    val tools: List<Tool>,
)