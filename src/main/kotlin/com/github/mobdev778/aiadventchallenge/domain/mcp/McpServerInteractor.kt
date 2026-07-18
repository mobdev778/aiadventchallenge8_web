package com.github.mobdev778.aiadventchallenge.domain.mcp

import com.github.mobdev778.aiadventchallenge.data.mcp.McpServerRepository
import com.github.mobdev778.aiadventchallenge.domain.agent.model.ToolResponse
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.ToolCall
import com.github.mobdev778.aiadventchallenge.domain.mcp.model.McpServer
import com.github.mobdev778.aiadventchallenge.domain.mymcp.MyMcpServerInteractor
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.sse.SSE
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.springframework.stereotype.Component

@Component
class McpServerInteractor(
    private val mcpServerRepository: McpServerRepository,
    private val myMcpServerInteractor: MyMcpServerInteractor,
    private val cachedChecker: CachedMcpToolsChecker,
) {

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val toolUrlMap = HashMap<String, String>()

    val allServersFlow: Flow<List<McpServer>> = combine(
        mcpServerRepository.observeServers(),
        myMcpServerInteractor.localServersFlow,
    ) { remoteServers, localServers ->
        remoteServers + localServers
    }.distinctUntilChanged()

    val activeToolsFlow : Flow<List<Tool>> = allServersFlow
        .map { servers ->
            val jobs = servers
                .map { server ->
                    scope.async {
                        try {
                            val tools = cachedChecker.loadTools(server.url)
                            tools.forEach { toolUrlMap[it.name] = server.url }
                            tools
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList()
                        }
                    }
                }
            val lists: List<List<Tool>> = jobs.awaitAll()
            lists.flatten()
        }

    suspend fun sendRequest(toolCall: ToolCall): ToolResponse? {
        println("!!! sendRequest($toolCall)")

        val url = toolUrlMap[toolCall.function.name] ?: return null
        val normalizedUrl = url.trim().removeSuffix("/")
        require(normalizedUrl.isNotEmpty()) { "MCP server URL must not be blank" }

        val httpClient = HttpClient(CIO) {
            install(SSE)
            install(HttpTimeout) {
                requestTimeoutMillis = 600_000
                connectTimeoutMillis = 600_000
                socketTimeoutMillis = 600_000
            }
        }

        try {
            return McpClientConnector.withClient(httpClient, normalizedUrl) { client ->
                sendRequest(client, toolCall)
            }
        } finally {
            httpClient.close()
        }
    }

    private suspend fun sendRequest(client: Client, toolCall: ToolCall): ToolResponse? {
        println("sendRequest($client, $toolCall)")

        // 1. Десериализуем строку аргументов от OpenAI в JsonObject, который требует MCP SDK
        val mcpArguments: JsonObject = try {
            Json.parseToJsonElement(toolCall.function.arguments).jsonObject
        } catch (e: Exception) {
            // Защита на случай, если LLM прислала невалидный JSON
            JsonObject(emptyMap())
        }

        // 2. Делаем вызов к MCP серверу через SDK
        // В зависимости от версии SDK, аргументы передаются либо вторым параметром, либо через объект CallToolRequest
        val result: CallToolResult = client.callTool(
            name = toolCall.function.name,
            arguments = mcpArguments
        )

        // 3. Вытаскиваем текстовый ответ из контента, который вернул MCP-сервер.
        // Сервер может возвращать массив элементов (текст, изображения и т.д.). Собираем весь текст в одну строку.
        val mergedContent = result.content.joinToString(separator = "\n") { contentElement ->
            when (contentElement) {
                is TextContent -> contentElement.text
                else -> "" // Игнорируем или обрабатываем другие типы (например, ImageContent), если нужно
            }
        }

        println("!!! merged content: $mergedContent")

        // 4. Мапим результат в твой чистый ToolResponse для OpenAI истории
        return ToolResponse(
            toolCallId = toolCall.id,
            name = toolCall.function.name,
            content = mergedContent
        )
    }
}
