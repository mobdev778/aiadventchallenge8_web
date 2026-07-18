package com.github.mobdev778.aiadventchallenge.domain.mymcp.model

/**
 * Состояние MCP-сервера, содержащее основные конфигурационные и статусные параметры.
 *
 * @param name Название сервера.
 * @param description Описание сервера.
 * @param isRunning Флаг, указывающий работает ли сервер в данный момент.
 * @param url URL-адрес сервера.
 * @param launchAtStartup Запускать ли сервер при старте приложения.
 */
data class MyMcpServerState(
    val name: String,
    val description: String,
    val isRunning: Boolean,
    val url: String,
    val launchAtStartup: Boolean,
)
