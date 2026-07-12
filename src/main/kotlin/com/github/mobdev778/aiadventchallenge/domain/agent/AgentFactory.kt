package com.github.mobdev778.aiadventchallenge.domain.agent

import com.github.mobdev778.aiadventchallenge.data.chatclient.repository.ChatClientRepository
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.Agent
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.ChatAssistantAgent
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentType
import com.github.mobdev778.aiadventchallenge.domain.mcp.McpServerInteractor
import kotlinx.coroutines.CoroutineScope
import java.util.UUID

class AgentFactory(
    private val chatClientRepository: ChatClientRepository,
    private val mcpServerInteractor: McpServerInteractor,
    private val coroutineScope: CoroutineScope,
    private val baseModel: String,
) {

    fun create(type: AgentType): Agent {
        val id = UUID.randomUUID().toString().replace("-", "")
        return when (type) {
            AgentType.ChatAssistant -> {
                ChatAssistantAgent(
                    id, chatClientRepository, mcpServerInteractor, coroutineScope, baseModel,
                )
            }
        }
    }
}