package com.github.mobdev778.aiadventchallenge.domain.agent.model

import java.util.UUID

data class AgentRequest(
    val chatId: UUID,
    val parentMessageId: UUID? = null,
    val time: Long,
    val query: String,
)