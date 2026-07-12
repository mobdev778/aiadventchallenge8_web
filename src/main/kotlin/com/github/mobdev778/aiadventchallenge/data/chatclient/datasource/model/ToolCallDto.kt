package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model

import kotlinx.serialization.Serializable

@Serializable
data class ToolCallDto(
    val id: String,
    val type: String,
    val function: FunctionCallDto
)
