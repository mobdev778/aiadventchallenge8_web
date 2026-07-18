package com.github.mobdev778.aiadventchallenge.domain.codereview

import com.github.mobdev778.aiadventchallenge.domain.agent.AgentOrchestrator
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.chat.model.Chat
import com.github.mobdev778.aiadventchallenge.domain.chat.persistence.ChatRepository
import com.github.mobdev778.aiadventchallenge.domain.chat.service.toEntity
import com.github.mobdev778.aiadventchallenge.domain.codereview.model.CodeReviewRequest
import com.github.mobdev778.aiadventchallenge.domain.codereview.model.CodeReviewResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class CodeReviewService(
    private val agentOrchestrator: AgentOrchestrator,
    private val chatRepository: ChatRepository,
) {

    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun postCodeReviewRequest(
        request: CodeReviewRequest
    ): CodeReviewResponse {
        println("!!! CodeReviewService: source: ${request.source}, target: ${request.target}")

        val now = Instant.now().toEpochMilli()
        val chat = Chat(
            id = UUID.randomUUID(),
            name = "/codereview ${request.source} ${request.target}",
            time = now,
            parentId = null,
        )
        println("!!! CodeReviewService: [1]")
        chatRepository.save(chat.toEntity())

        println("!!! CodeReviewService: [2]")

        val request = AgentRequest(
            chatId = chat.id,
            parentMessageId = null,
            time = now,
            query = "/codereview ${request.source} ${request.target}",
        )

        println("!!! CodeReviewService: [3]")

        val responsesDeferred = backgroundScope.async {
            agentOrchestrator.responses
                .filter { response -> response.request.chatId == chat.id }
                .take(1)
                .toList()
                .first()
        }

        println("!!! CodeReviewService: [4]")

        agentOrchestrator.asyncRequest(request)

        println("!!! CodeReviewService: [5]")

        val response = responsesDeferred.await()
        val jsonResponse = if (response.message.isEmpty() || response.message.contains("Одобрено")) {
            CodeReviewResponse(
                status = "success",
                message = response.message,
            )
        } else {
            CodeReviewResponse(
                status = "error",
                message = response.message,
            )
        }

        println("!!! CodeReviewService: [6]")

        println("!!! CodeReviewService: result: $jsonResponse")
        return jsonResponse
    }
}