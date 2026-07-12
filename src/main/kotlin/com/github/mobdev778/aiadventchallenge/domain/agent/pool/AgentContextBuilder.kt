package com.github.mobdev778.aiadventchallenge.domain.agent.pool

import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentContext
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.chat.persistence.ChatMessageRepository
import com.github.mobdev778.aiadventchallenge.domain.chat.service.toModel
import com.github.mobdev778.aiadventchallenge.domain.mcp.McpServerInteractor
import com.github.mobdev778.aiadventchallenge.domain.profile.model.Profile
import kotlinx.coroutines.flow.first

class AgentContextBuilder(
    private val chatMessageRepository: ChatMessageRepository,
    private val mcpServerInteractor: McpServerInteractor,
) {

    suspend fun build(request: AgentRequest): AgentContext {
        val profile = Profile.default
        val windowMessages = chatMessageRepository.findAllByChatIdOrderByTimeAsc(request.chatId)
            .filter { it.id != request.parentMessageId }
            .takeLast(4)
            .map { it.toModel() }

        val tools = mcpServerInteractor.activeToolsFlow.first()

        println("!!! tools: $tools")

        return AgentContext(
            profile = profile,
            messages = windowMessages,
            tools = tools,
        )
    }
}