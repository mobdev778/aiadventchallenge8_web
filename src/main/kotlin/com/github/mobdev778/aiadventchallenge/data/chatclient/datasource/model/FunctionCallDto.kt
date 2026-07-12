package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model

import kotlinx.serialization.Serializable

@Serializable
data class FunctionCallDto(
    val name: String,
    val arguments: String // JSON-строка с аргументами
)
