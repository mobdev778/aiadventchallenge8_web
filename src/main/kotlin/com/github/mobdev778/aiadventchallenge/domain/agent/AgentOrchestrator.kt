package com.github.mobdev778.aiadventchallenge.domain.agent

import com.github.mobdev778.aiadventchallenge.data.chatclient.repository.ChatClientRepository
import com.github.mobdev778.aiadventchallenge.data.mcp.McpServerRepository
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.Agent
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentContext
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentResponse
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentType
import com.github.mobdev778.aiadventchallenge.domain.agent.pool.AgentContextBuilder
import com.github.mobdev778.aiadventchallenge.domain.agent.pool.AgentPool
import com.github.mobdev778.aiadventchallenge.domain.chat.persistence.ChatMessageRepository
import com.github.mobdev778.aiadventchallenge.domain.mcp.CachedMcpToolsChecker
import com.github.mobdev778.aiadventchallenge.domain.mcp.McpServerInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AgentOrchestrator(
    private val mcpServerInteractor: McpServerInteractor,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatClientRepository: ChatClientRepository,
    @Value("\${ai.chat.base-model}")
    private val baseModel: String,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val agentContextBuilder = AgentContextBuilder(
        chatMessageRepository, mcpServerInteractor,
    )

    private val factory = AgentFactory(
        chatClientRepository, mcpServerInteractor, scope, baseModel
    )

    val responses = MutableSharedFlow<AgentResponse>(
        extraBufferCapacity = 1
    )

    val onResponseReady: suspend (AgentResponse) -> Unit = { response ->
        responses.tryEmit(response)
    }

    private val pools by lazy {
        AgentType.entries
            .map { type ->
                val agents = mutableListOf<Agent>()
                repeat(AGENTS_PER_POOL) { agents.add(factory.create(type)) }
                AgentPool(type, agents) { response ->
                    onResponseReady(response)
                }
            }
            .associateBy { it.type }
    }

    private companion object {
        const val AGENTS_PER_POOL = 3
    }

    /**
     * Маршрутизация: отправляет сообщение конкретному пулу агентов
     */
    fun asyncRequest(request: AgentRequest) {
        scope.launch {
            println("Оркестратору поступил запрос: ${request.query}")
            val context = agentContextBuilder.build(request)
            val agentType = getAgentType(context)
            println("Определен тип агента: $agentType")
            val pool = pools[agentType] ?: throw IllegalArgumentException("Unknown agent type")
            println("Выбран пул агентов: $pool")
            pool.enqueue(context, request)
        }
    }

    /**
     * Запуск всех агентов
     */
    fun startAgents() {
        pools.values.forEach { pool ->
            pool.start(scope)
        }
    }

    /**
     * Приостановка/остановка всех агентов
     */
    fun stopAgents() {
        pools.values.forEach { pool ->
            pool.stop()
        }
    }

    private fun getAgentType(context: AgentContext): AgentType {
        // в нашей задаче только 1 тип агента
        return AgentType.ChatAssistant
    }
}