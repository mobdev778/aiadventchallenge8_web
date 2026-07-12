package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FinishReasonDto {
    /** Модель успешно завершила генерацию текста и встретила маркер конца строки (end-of-token). */
    @SerialName("stop")
    Stop,

    /** Генерация прервана, так как был достигнут лимит токенов (max_tokens). */
    @SerialName("length")
    Length,

    /** Модель решила вызвать внешние функции или инструменты (функции/плагины). */
    @SerialName("tool_calls")
    ToolCalls,

    /** Текст прерван из-за срабатывания фильтра безопасности или цензуры контента. */
    @SerialName("content_filter")
    ContentFilter,

    /** Генерация остановлена из-за достижения специфического лимита токенов для механизма RLE. */
    @SerialName("rle_max_tokens")
    RleMaxTokens,
}
