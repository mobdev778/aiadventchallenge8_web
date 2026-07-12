package com.github.mobdev778.aiadventchallenge.domain.chatclient.model

import kotlinx.serialization.SerialName

enum class Role {
    /** Системные инструкции, определяющие поведение и ограничения модели. */
    @SerialName("system")
    System,

    /** Сообщения и вопросы от пользователя. */
    @SerialName("user")
    User,

    /** Ответы, сгенерированные самой языковой моделью. */
    @SerialName("assistant")
    Assistant,

    /** Результат работы внешней функции (инструмента), переданный обратно модели. */
    @SerialName("tool")
    Tool,

    /** Инструкции разработчика (используются вместо system в некоторых новых моделях вроде OpenAI o1/o3). */
    @SerialName("developer")
    Developer,
}
