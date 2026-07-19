package com.github.mobdev778.aiadventchallenge.domain.mymcp.crm

import com.github.mobdev778.aiadventchallenge.domain.mymcp.BaseMyMcpServer
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.CallToolRequest
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.springframework.stereotype.Component

/**
 * Локальный MCP-сервер, предоставляющий AI-агенту инструменты для работы с данными CRM-системы
 * поддержки пользователей. Данные читаются из файла [support_data.json](src/main/resources/static/support_data.json).
 *
 * Сервер работает поверх протокола [Model Context Protocol](https://modelcontextprotocol.io)
 * и предоставляет три инструмента: "get_ticket_by_id", "get_user_by_id",
 * "search_tickets_by_user".
 *
 * Наследуется от [BaseMyMcpServer], используя общую инфраструктуру запуска и конфигурации
 * MCP-серверов.
 */
@Component
class MyMcpCrmServer : BaseMyMcpServer(
    name = "MyMcpCrmServer",
    description = "Локальный MCP-сервер для доступа к данным CRM-системы поддержки пользователей",
    port = 3010,
    launchAtStartup = true,
) {

    /**
     * Загруженные и распарсенные данные из support_data.json.
     * Используется ленивая инициализация: файл читается при первом обращении к данным.
     */
    private val supportData: JsonObject by lazy {
        val jsonString = javaClass.classLoader
            .getResourceAsStream("static/support_data.json")
            ?.bufferedReader()
            ?.readText()
            ?: error("support_data.json not found on classpath")
        Json.parseToJsonElement(jsonString).jsonObject
    }

    /**
     * Кешированный массив пользователей из JSON-данных.
     */
    private val users: List<JsonObject> by lazy {
        supportData["users"]?.jsonArray?.map { it.jsonObject } ?: emptyList()
    }

    /**
     * Кешированный массив тикетов из JSON-данных.
     */
    private val tickets: List<JsonObject> by lazy {
        supportData["tickets"]?.jsonArray?.map { it.jsonObject } ?: emptyList()
    }

    /**
     * Создаёт и конфигурирует экземпляр [Server] — ядро MCP-сервера.
     *
     * В процессе создания:
     * - Устанавливаются метаданные сервера (название, версия).
     * - Объявляются поддерживаемые возможности (tools).
     * - Регистрируются три инструмента:
     *   - `get_ticket_by_id` — возвращает полную информацию о тикете по его ID.
     *   - `get_user_by_id` — возвращает профиль пользователя по его ID.
     *   - `search_tickets_by_user` — возвращает все тикеты конкретного пользователя.
     * - Задаются инструкции для AI-ассистента.
     *
     * @return Сконфигурированный и готовый к запуску экземпляр [Server].
     */
    override fun createServer(): Server {
        val server = Server(
            serverInfo = Implementation(
                name = "my-mcp-crm-server",
                version = "1.0.0",
            ),
            options = ServerOptions(
                capabilities = ServerCapabilities(
                    tools = ServerCapabilities.Tools(listChanged = false),
                ),
            ),
            instructions = "Local MCP server for CRM support data access. " +
                    "CRITICAL RULES FOR THE ASSISTANT:\n" +
                    "1. Use 'get_ticket_by_id' to get full ticket details, including system_logs and message history.\n" +
                    "2. Use 'get_user_by_id' to get user profile: name, email, tier, and active device info.\n" +
                    "3. Use 'search_tickets_by_user' to find all tickets belonging to a specific user.\n" +
                    "4. All results are returned as JSON-formatted strings.",
        )

        addGetUserByIdTool(server)
        addSearchTicketByUserTool(server)

        return server
    }

    /**
     * Регистрирует инструмент `get_user_by_id` — возвращает профиль пользователя:
     * имя, email, тариф (tier) и параметры устройства (active_device).
     *
     * @param server Экземпляр MCP-сервера, на котором регистрируется инструмент.
     */
    private fun addGetUserByIdTool(server: Server) {
        server.addTool(
            name = "get_user_by_id",
            description = "Возвращает данные клиента: его имя, email, тариф (tier) и параметры устройства " +
                    "(active_device). Нужен, чтобы понять, на каком устройстве (например, iOS 4.2.1) " +
                    "произошла ошибка.",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("user_id", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put(
                            "description",
                            JsonPrimitive("Идентификатор пользователя, например 'usr_94821'")
                        )
                    })
                },
                required = listOf("user_id"),
            ),
        ) { request: CallToolRequest ->
            val userId = request.params.arguments
                ?.get("user_id")
                ?.jsonPrimitive
                ?.content
                ?: return@addTool textResult("Error: 'user_id' parameter is required.")

            val user = users.find { it["user_id"]?.jsonPrimitive?.content == userId }
            if (user == null) {
                textResult("Error: User with ID '$userId' not found.")
            } else {
                textResult(user.toString())
            }
        }
    }

    /**
     * Регистрирует инструмент `search_ticket_by_user` — находит тикет
     * конкретного пользователя.
     *
     * @param server Экземпляр MCP-сервера, на котором регистрируется инструмент.
     */
    private fun addSearchTicketByUserTool(server: Server) {
        server.addTool(
            name = "search_ticket_by_user",
            description = "Находит все открытые или закрытые тикеты конкретного пользователя. " +
                    "Полезно, если клиент пишет «У меня опять та же проблема», чтобы LLM могла " +
                    "посмотреть историю прошлых обращений.",
            inputSchema = ToolSchema(
                properties = buildJsonObject {
                    put("user_id", buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put(
                            "description",
                            JsonPrimitive("Идентификатор пользователя, например 'usr_94821'")
                        )
                    })
                },
                required = listOf("user_id"),
            ),
        ) { request: CallToolRequest ->
            val userId = request.params.arguments
                ?.get("user_id")
                ?.jsonPrimitive
                ?.content
                ?: return@addTool textResult("Error: 'user_id' parameter is required.")

            val userTickets = tickets.filter {
                it["user_id"]?.jsonPrimitive?.content == userId
            }

            if (userTickets.isEmpty()) {
                textResult("No tickets found for user ID '$userId'.")
            } else {
                textResult(userTickets.first().toString())
            }
        }
    }

    /**
     * Оборачивает текстовую строку в [CallToolResult], пригодный для возврата MCP-клиенту.
     *
     * @param text Текстовое содержимое результата.
     * @return Объект [CallToolResult] с единственным элементом [TextContent] и флагом `isError = false`.
     */
    private fun textResult(text: String): CallToolResult =
        CallToolResult(
            content = listOf(TextContent(text = text)),
            isError = false,
        )
}
