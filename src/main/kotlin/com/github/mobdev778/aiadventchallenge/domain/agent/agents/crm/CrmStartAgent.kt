package com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm

import com.github.mobdev778.aiadventchallenge.data.chatclient.repository.ChatClientRepository
import com.github.mobdev778.aiadventchallenge.domain.agent.AgentOrchestrator
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.BaseAgent
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context.CrmContext
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context.CrmContextRegistry
import com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context.CrmUser
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentContext
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentResponse
import com.github.mobdev778.aiadventchallenge.domain.mcp.McpServerInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

class CrmStartAgent(
    id: String,
    chatClientRepository: ChatClientRepository,
    mcpServerInteractor: McpServerInteractor,
    coroutineScope: CoroutineScope,
    baseModel: String,
) : BaseAgent(
    id, chatClientRepository, mcpServerInteractor, coroutineScope, baseModel,
) {

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    override suspend fun handle(
        agentOrchestrator: AgentOrchestrator,
        context: AgentContext,
        request: AgentRequest
    ): AgentResponse? {
        val userId = request.query.substringAfter("/crm ")
        println("!!! CrmStartAgent. userId: $userId")

        val response = sendMessage(
            context,
            getSystemPrompt(),
            request.copy(query = userId),
        )
        val user: CrmUser? = try {
            json.decodeFromString<CrmUser>(response.message)
        } catch (e: Exception) {
            null
        }

        return if (user != null) {
            CrmContextRegistry.put(request.chatId, CrmContext(user, null))
            agentOrchestrator.asyncRequest(
                request.copy(query = "Найди мою заявку")
            )
            null
        } else {
            response.copy(message = "Пользователь не найден в системе")
        }
    }

    private fun getSystemPrompt() = """
        Ты - CRM-помощник, который используя MCP-инструмент 'get_user_by_id',
        должен найти по введенному пользователем идентификатору информацию о пользователе в виде JSON и вернуть
        этот JSON-объект "как есть".
        В противном случае верни ответ [NO RESPONSE].
    """.trimIndent()
}