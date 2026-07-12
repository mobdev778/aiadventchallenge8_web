package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model

import kotlinx.serialization.Serializable

@Serializable
data class PlanningResponseDto(
    val isTask: Boolean,
    val taskName: String? = null,
    val plan: List<String>? = null
)
