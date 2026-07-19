package com.github.mobdev778.aiadventchallenge.domain.agent.agents

import com.github.mobdev778.aiadventchallenge.data.chatclient.repository.ChatClientRepository
import com.github.mobdev778.aiadventchallenge.domain.agent.AgentOrchestrator
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentContext
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentResponse
import com.github.mobdev778.aiadventchallenge.domain.mcp.McpServerInteractor
import kotlinx.coroutines.CoroutineScope

class ChatAssistantAgent(
    id: String,
    chatClientRepository: ChatClientRepository,
    mcpServerInteractor: McpServerInteractor,
    coroutineScope: CoroutineScope,
    baseModel: String,
) : BaseAgent(
    id, chatClientRepository, mcpServerInteractor, coroutineScope, baseModel,
) {

    override suspend fun handle(
        agentOrchestrator: AgentOrchestrator,
        context: AgentContext,
        request: AgentRequest
    ): AgentResponse? {
        return super.handle(agentOrchestrator, context, request)
    }
}