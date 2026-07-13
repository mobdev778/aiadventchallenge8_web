package com.github.mobdev778.aiadventchallenge.web

import com.github.mobdev778.aiadventchallenge.domain.mcp.McpToolsChecker
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
class McpToolsCheckerTest {

    @Test
    fun testQdrant() {
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
}