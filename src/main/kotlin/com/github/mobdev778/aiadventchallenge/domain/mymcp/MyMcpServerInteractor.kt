package com.github.mobdev778.aiadventchallenge.domain.mymcp

import com.github.mobdev778.aiadventchallenge.domain.mcp.model.McpServer
import com.github.mobdev778.aiadventchallenge.domain.mymcp.crm.MyMcpCrmServer
import com.github.mobdev778.aiadventchallenge.domain.mymcp.file.MyMcpFileServer
import com.github.mobdev778.aiadventchallenge.domain.mymcp.git.MyMcpGitServer
import com.github.mobdev778.aiadventchallenge.domain.mymcp.model.MyMcpServerState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.UUID

@Component
class MyMcpServerInteractor(
    private val gitServer: MyMcpGitServer,
    private val fileServer: MyMcpFileServer,
    private val mcpCrmServer: MyMcpCrmServer,
) {

    private val servers: List<MyMcpServer> = listOf(
        gitServer,
        fileServer,
        mcpCrmServer,
    )

    val localServersFlow: Flow<List<McpServer>> = combine(
        servers.map {
            it.observeState()
        }
    ) {
        it.map { server ->
            McpServer(
                id = UUID.nameUUIDFromBytes(server.url.toByteArray(StandardCharsets.UTF_8)),
                name = server.name,
                url = server.url,
            )
        }.toList()
    }

    val serverStatesFlow: Flow<List<MyMcpServerState>> = combine(
        servers.map { it.observeState() }
    ) {
        it.toList()
    }

    suspend fun start(name: String) {
        val server = servers.firstOrNull { it.observeState().first().name == name }
        server?.start()
    }

    suspend fun stop(name: String) {
        val server = servers.firstOrNull { it.observeState().first().name == name }
        server?.stop()
    }
}