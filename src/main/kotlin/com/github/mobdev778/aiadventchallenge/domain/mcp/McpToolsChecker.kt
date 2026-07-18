package com.github.mobdev778.aiadventchallenge.domain.mcp

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.sse.SSE
import io.modelcontextprotocol.kotlin.sdk.client.mcpSse
import io.modelcontextprotocol.kotlin.sdk.types.ListToolsRequest
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Component

@Component
class McpToolsChecker {

    suspend fun loadTools(url: String): List<Tool> = withContext(Dispatchers.IO) {
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
            try {
                McpClientConnector.withClient(httpClient, normalizedUrl) {
                    it.listTools(ListToolsRequest()).tools
                }
            } catch (connectorError: Exception) {
                // Fallback: if Streamable HTTP auto-detection failed, try SSE transport directly
                // This handles cases like Qdrant SSE servers where StreamableHttpError
                // doesn't contain the expected fallback trigger messages
                val sseClient = httpClient.mcpSse(normalizedUrl)
                try {
                    sseClient.listTools(ListToolsRequest()).tools
                } finally {
                    try {
                        sseClient.close()
                    } catch (_: NoSuchMethodError) {
                        // kotlinx-coroutines version mismatch; safe to ignore
                    } catch (_: Exception) {
                        // Ignore other close errors
                    }
                }
            }
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
