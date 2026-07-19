package com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context

import kotlinx.serialization.Serializable

@Serializable
data class CrmTicketMessage(
    val sender: String,
    val timestamp: String,
    val text: String,
)