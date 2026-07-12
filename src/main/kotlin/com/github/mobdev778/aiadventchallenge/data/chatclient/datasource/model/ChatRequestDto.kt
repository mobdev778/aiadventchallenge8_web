package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequestDto(
    /** Название модели, которую нужно использовать для генерации (например, "gpt-4o"). */
    val model: String,

    /** Уровень усилий, затрачиваемых моделью на внутренние рассуждения перед ответом (для моделей o-серии). */
    @SerialName("reasoning_effort")
    val reasoningEffort: ReasoningEffortDto? = null,

    /** Список сообщений, составляющих историю диалога. */
    val messages: List<MessageDto>,

    /** Список доступных MCP-инструментов. */
    val tools: List<ToolDto>? = null,

    /** Режим выбора инструмента. При наличии инструментов всегда используется auto. */
    @SerialName("tool_choice")
    val toolChoice: String? = null,

    /** Температура генерации (от 0.0 до 2.0). Выше значение — креативнее ответ, ниже — точнее и предсказуемее. */
    val temperature: Double,

    /** Максимальное количество токенов, которое модель может сгенерировать в ответе. */
    @SerialName("max_tokens")
    val maxTokens: Int? = null,

    /** Список стоп-последовательностей (до 4 строк). При встрече любой из них модель прекратит генерацию. */
    val stop: List<String>? = null,
)
