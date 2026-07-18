package com.github.mobdev778.aiadventchallenge.web

import com.github.mobdev778.aiadventchallenge.domain.mcp.McpToolsChecker
import com.github.mobdev778.aiadventchallenge.domain.mymcp.file.MyMcpFileServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
class McpFileServerTest {

    @Test
    fun testFileServer() = runBlocking(Dispatchers.Default) {
        val fileServer = MyMcpFileServer()
        fileServer.start()
        try {
            launch {
                val tools = McpToolsChecker().loadTools("http://127.0.0.1:3008/sse")
                tools.forEach {
                    println("name: ${it.name}")
                    println("title: ${it.title}")
                    println("description: ${it.description}")
                    println("inputSchema: ${it.inputSchema}")
                }
            }

            delay(3000)
        } finally {
            fileServer.stop()
        }
    }
}