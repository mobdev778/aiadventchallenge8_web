package com.github.mobdev778.aiadventchallenge.domain.mcp

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.sse.SSE
import io.modelcontextprotocol.kotlin.sdk.client.mcpSse
import io.modelcontextprotocol.kotlin.sdk.types.ListToolsRequest
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

fun main() {
    // Проверка ответа "tools" для Qdrant:
    runBlocking {
        val tools = McpToolsChecker().loadTools("http://localhost:8000/sse")
        tools.forEach {
            println("name: ${it.name}")
            println("title: ${it.title}")
            println("description: ${it.description}")
            println("inputSchema: ${it.inputSchema}")
        }
    }
}

@Component
class McpToolsChecker {

    suspend fun loadTools(url: String): List<Tool> = withContext(Dispatchers.IO) {
        val normalizedUrl = url.trim().removeSuffix("/")
        require(normalizedUrl.isNotEmpty()) { "MCP server URL must not be blank" }

        val httpClient = HttpClient(CIO) {
            install(SSE)
        }

        try {
            val client = httpClient.mcpSse(normalizedUrl) {
                // headers["Accept"] = "application/json, text/event-stream"
            }
            client.listTools(ListToolsRequest()).tools
        } catch (error: Throwable) {
            error.printStackTrace()
            throw IllegalStateException(error.toReadableMessage(normalizedUrl), error)
        } finally {
            httpClient.close()
        }
    }

    private fun Throwable.toReadableMessage(url: String): String {
        val rootCause = generateSequence(this) { it.cause }.last()
        val rootMessage = rootCause.message?.takeIf { it.isNotBlank() }
        val rootType = rootCause::class.qualifiedName.orEmpty()

        return when {
            this is IllegalArgumentException && message?.contains("Failed to prepare request") == true ->
                "Не удалось подготовить HTTP-запрос к $url. Вероятна несовместимость версий Ktor в classpath."

            rootType.contains("HttpTimeout") ->
                "Таймаут при подключении к $url"

            rootMessage != null -> "Ошибка подключения к $url: $rootMessage"
            else -> "Ошибка подключения к $url: ${rootCause::class.simpleName ?: "Unknown error"}"
        }
    }
}
