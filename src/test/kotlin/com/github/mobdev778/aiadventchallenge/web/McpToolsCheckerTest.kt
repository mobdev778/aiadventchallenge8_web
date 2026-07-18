package com.github.mobdev778.aiadventchallenge.web

import com.github.mobdev778.aiadventchallenge.domain.mcp.McpToolsChecker
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

// @Ignore
class McpToolsCheckerTest {

    @Test
    fun testQdrant() = runBlocking {
        // Проверка ответа "tools" для Qdrant:
        val tools = McpToolsChecker().loadTools("http://localhost:8000/sse")
        val find = tools.firstOrNull { it.name == "qdrant-find" }
        assertEquals("qdrant-find", find?.name)
    }
}