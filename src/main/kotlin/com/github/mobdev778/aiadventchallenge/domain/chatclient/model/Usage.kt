package com.github.mobdev778.aiadventchallenge.domain.chatclient.model

data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)
