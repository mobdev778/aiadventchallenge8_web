package com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CrmTicket(
    @SerialName("ticket_id") val ticketId: String,
    @SerialName("user_id") val userId: String,
    val subject: String,
    val status: String,
    val priority: String,
    @SerialName("created_at") val createdAt: String,
    val category: String,
    @SerialName("system_logs") val systemLogs: String,
    val messages: List<CrmTicketMessage> = emptyList(),
)