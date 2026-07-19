package com.github.mobdev778.aiadventchallenge.domain.agent

import com.github.mobdev778.aiadventchallenge.data.chatclient.repository.ChatClientRepository
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.Agent
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.ChatAssistantAgent
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.CodeReviewAgent
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.HelpAgent
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.CrmChatAgent
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.CrmStartAgent
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
        val id = type.name + "-" + UUID.randomUUID().toString().replace("-", "")
        return when (type) {
            AgentType.Help -> {
                HelpAgent(
                    id, chatClientRepository, mcpServerInteractor, coroutineScope, baseModel,
                )
            }
            AgentType.ChatAssistant -> {
                ChatAssistantAgent(
                    id, chatClientRepository, mcpServerInteractor, coroutineScope, baseModel,
                )
            }
            AgentType.CodeReview -> {
                CodeReviewAgent(
                    id, chatClientRepository, mcpServerInteractor, coroutineScope, baseModel,
                )
            }

            AgentType.CrmStart -> {
                CrmStartAgent(
                    id, chatClientRepository, mcpServerInteractor, coroutineScope, baseModel,
                )
            }
            AgentType.CrmChat -> {
                CrmChatAgent(
                    id, chatClientRepository, mcpServerInteractor, coroutineScope, baseModel,
                )
            }
        }
    }
}