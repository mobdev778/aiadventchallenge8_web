package com.github.mobdev778.aiadventchallenge.domain.mcp

import io.ktor.client.HttpClient
import io.modelcontextprotocol.kotlin.sdk.client.Client
import io.modelcontextprotocol.kotlin.sdk.client.StreamableHttpError
import io.modelcontextprotocol.kotlin.sdk.client.mcpSse
import io.modelcontextprotocol.kotlin.sdk.client.mcpStreamableHttp

/**
 * Helper for creating MCP clients with automatic transport detection.
 *
 * Strategy:
 * 1. Try [mcpStreamableHttp] — preferred for Streamable HTTP servers (e.g. external Qdrant)
 * 2. Fall back to [mcpSse] on [StreamableHttpError] indicating an SSE-only server
 *    (e.g. "Method Not Allowed", "sessionId query parameter is not provided")
 * 3. [NoSuchMethodError] during client close is suppressed (kotlinx-coroutines version mismatch)
 */
object McpClientConnector {

    /**
     * Creates an MCP client and executes [block], handling transport auto-detection.
     * The returned client is closed before this function returns.
     *
     * @param httpClient Pre-configured [HttpClient] (typically CIO engine with timeouts)
     * @param url Normalized MCP server URL
     * @param block Operation to perform with the connected client
     * @return Result of [block]
     */
    suspend fun <T> withClient(
        httpClient: HttpClient,
        url: String,
        block: suspend (Client) -> T,
    ): T {
        var streamableClient: Client? = null
        var sseClient: Client? = null

        try {
            // Try Streamable HTTP first
            streamableClient = httpClient.mcpStreamableHttp(url)
            return block(streamableClient)
        } catch (e: Exception) {
            if (isStreamableHttpFallbackError(e)) {
                // Close the failed streamable client silently
                safelyClose(streamableClient)
                streamableClient = null

                // Fall back to SSE
                sseClient = httpClient.mcpSse(url)
                return block(sseClient)
            }
            throw e
        } finally {
            safelyClose(streamableClient)
            safelyClose(sseClient)
        }
    }

    private fun isStreamableHttpFallbackError(e: Throwable): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            if (cause is StreamableHttpError) {
                val msg = cause.message ?: ""
                if ("Method Not Allowed" in msg || "sessionId" in msg) {
                    return true
                }
            }
            cause = cause.cause
        }
        return false
    }

    private suspend fun safelyClose(client: Client?) {
        if (client == null) return
        try {
            client.close()
        } catch (_: NoSuchMethodError) {
            // kotlinx-coroutines version mismatch — Job.cancel$default not found; safe to ignore
        } catch (_: Exception) {
            // Ignore other close errors
        }
    }
}
