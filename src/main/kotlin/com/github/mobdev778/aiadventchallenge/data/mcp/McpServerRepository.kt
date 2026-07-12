package com.github.mobdev778.aiadventchallenge.data.mcp

import com.github.mobdev778.aiadventchallenge.domain.mcp.model.McpServer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class McpServerRepository {

    private val servers = listOf(
        McpServer(
            name = "Qdrant",
            url = "http://localhost:8000/sse"
        )
    )

    fun observeServers(): Flow<List<McpServer>> = flowOf<List<McpServer>>(servers)

    suspend fun getServer(serverId: UUID): McpServer? = servers.firstOrNull { it.id == serverId }
}