package com.github.mobdev778.aiadventchallenge.domain.chatclient.model

data class FunctionCall(
    val name: String,
    val arguments: String // JSON-строка с аргументами
)
