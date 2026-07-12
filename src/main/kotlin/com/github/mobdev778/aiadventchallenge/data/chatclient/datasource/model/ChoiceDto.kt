package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChoiceDto(
    /** Индекс варианта ответа в списке (начиная с 0). */
    val index: Int,

    /** Объект сообщения, сгенерированный моделью (содержит роль "assistant" и текст ответа). */
    val message: MessageDto,

    /** Причина, по которой модель прекратила генерацию токенов. */
    @SerialName("finish_reason")
    val finishReason: FinishReasonDto,
)
