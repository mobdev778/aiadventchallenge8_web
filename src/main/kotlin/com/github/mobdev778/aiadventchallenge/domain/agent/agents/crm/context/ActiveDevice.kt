package com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ActiveDevice(
    val platform: String,
    @SerialName("app_version") val appVersion: String,
    @SerialName("os_version") val osVersion: String,
)