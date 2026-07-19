package com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CrmUser(
    @SerialName("user_id") val userId: String,
    val name: String,
    val email: String,
    val tier: String,
    @SerialName("registration_date") val registrationDate: String,
    @SerialName("active_device") val activeDevice: ActiveDevice,
)