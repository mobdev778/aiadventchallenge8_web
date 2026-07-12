package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponseDto(
    /** Уникальный идентификатор данной генерации (ответа чата). */
    val id: String,

    /** Название модели, которая использовалась для генерации ответа. */
    val model: String,

    /** Список вариантов ответов, сгенерированных моделью. */
    val choices: List<ChoiceDto>,

    /** Данные об использовании токенов для этого запроса. */
    val usage: UsageDto? = null
)
