package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UsageDto(
    /** Количество токенов во входном запросе (prompt). */
    @SerialName("prompt_tokens")
    val promptTokens: Int,

    /** Количество токенов в сгенерированном ответе (completion). */
    @SerialName("completion_tokens")
    val completionTokens: Int,

    /** Общее количество потраченных токенов (prompt_tokens + completion_tokens). */
    @SerialName("total_tokens")
    val totalTokens: Int
)
