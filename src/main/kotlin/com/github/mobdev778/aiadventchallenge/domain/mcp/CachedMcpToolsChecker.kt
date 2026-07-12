package com.github.mobdev778.aiadventchallenge.domain.mcp

import io.modelcontextprotocol.kotlin.sdk.types.Tool
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class CachedMcpToolsChecker(
    private val rawChecker: McpToolsChecker,
) {

    private val cache = ConcurrentHashMap<String, List<Tool>>()

    suspend fun loadTools(url: String): List<Tool> {
        return cache.getOrPut(url) {
            rawChecker.loadTools(url)
        }
    }
}