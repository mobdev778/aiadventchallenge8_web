package com.github.mobdev778.aiadventchallenge.domain.agent.pool

import com.github.mobdev778.aiadventchallenge.domain.agent.agents.Agent
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentContext
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentResponse
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Пул агентов конкретного типа
 */
class AgentPool<A : Agent>(
    val type: AgentType,
    val agents: List<A>,
    val onResponseReady: suspend (AgentResponse) -> Unit
) {

    private val queue = AgentRequestQueue()

    private val activeJobs = mutableListOf<Job>()

    fun enqueue(context: AgentContext, request: AgentRequest) {
        queue.enqueue(context, request)
    }

    fun start(scope: CoroutineScope) {
        if (activeJobs.isNotEmpty()) {
            return
        }

        agents.forEach { agent ->
            val job = scope.launch(Dispatchers.Default) {
                try {
                    while (isActive) {
                        val (context, request) = queue.dequeue() // Берут задачу по очереди (кто первый успел)
                        println("Агенту: $agent пришло новое сообщение: ${request.query}")
                        val response = agent.handle(context, request)
                        onResponseReady(response)
                    }
                } catch (e: CancellationException) {
                    throw e
                    // Нормальное завершение при остановке
                } finally {
                    println("[Система] Агент ${agent::class.simpleName} #${agent.id} остановлен.")
                }
            }
            activeJobs.add(job)
        }
        println("[Пул $type] Запущен. Активно агентов: ${agents.size}")
    }

    /**
     * Поставить пул на паузу (остановить корутины, но задачи в очереди сохранятся)
     */
    fun stop() {
        activeJobs.forEach { it.cancel() }
        activeJobs.clear()
        println("[Пул $type] Полностью остановлен.")
    }

    override fun toString(): String {
        return "[Пул агентов]: $agents"
    }
}
