package com.github.mobdev778.aiadventchallenge.domain.agent.agents

import com.github.mobdev778.aiadventchallenge.data.chatclient.repository.ChatClientRepository
import com.github.mobdev778.aiadventchallenge.domain.agent.AgentOrchestrator
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentContext
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentResponse
import com.github.mobdev778.aiadventchallenge.domain.mcp.McpServerInteractor
import kotlinx.coroutines.CoroutineScope

class CodeReviewAgent(
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
        val query = request.query.substringAfter("/codereview ")
        val (source, target) = query.split(" ")
        println("!!! CodeReviewAgent. query: $query")
        return sendMessage(
            context,
            getSystemPrompt(source, target),
            request.copy(query = "source: $source, target: $target"),
        )
    }

    private fun getSystemPrompt(source: String, target: String) = """
        Вы Senior Fullstack-разработчик. Проведите аудит изменений между `$source` и $target.
        Доступные инструменты: git_diff (анализ изменений), project_tree/read_file (контекст файлов), qdrant-find/qdrant-store (правила и гайдлайны RAG).

        АЛГОРИТМ:
        1. Запросите `git_diff` для `$source $target`.
        2. Извлеките ключевые сущности из диффа и найдите правила проекта через `qdrant-find`.
        3. При необходимости читайте файлы целиком через `read_file`.
        4. Оцените код-стайл, баги, безопасность, производительность и соответствие RAG.

        СТРУКТУРА ОТЧЕТА:
        ## 📊 Краткая сводка изменений: [Суть фичи/фикса и число файлов]
        ## 🛠️ Архитектурное соответствие (RAG): [Соответствие правилам или "Специфичных правил в базе знаний не обнаружено"]
        ## 🔍 Критические замечания (Блокеры / Баги): [Критические проблемы или "Критических замечаний нет"]
        ## 💡 Рекомендации по улучшению (Минорные правки): [Оптимизация, читаемость, тесты]
        ## 🎯 Вердикт: [❌ Отклонить / ✅ Одобрено (LGTM)]

        ПРАВИЛА: Вежливость, примеры кода, вердикт "Одобрено" при наличии только минорных правок. Не додумывайте логику — используйте `read_file`.
    """.trimIndent()
}