package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model

import kotlinx.serialization.Serializable

@Serializable
data class ToolDto(
    val type: String,
    val function: FunctionDto,
)
