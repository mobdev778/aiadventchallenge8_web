package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDto(
    /** Роль автора сообщения (например: "system", "user", "assistant" или "tool"). */
    val role: RoleDto,

    /** Текст сообщения. Может быть null, если модель вместо текста возвращает вызов функции (tool_calls). */
    val content: String?,

    /**
     * Заполняется ТОЛЬКО когда role == ASSISTANT и модель вызывает инструмент
     */
    @SerialName("tool_calls")
    val toolCalls: List<ToolCallDto>? = null,

    /**
     * Заполняется ТОЛЬКО когда role == TOOL (результат выполнения MCP)
     */
    @SerialName(value = "tool_call_id")
    val toolCallId: String? = null,

    /**
     * Заполняется ТОЛЬКО когда role == TOOL (имя вызванной функции)
     */
    val name: String? = null
)
