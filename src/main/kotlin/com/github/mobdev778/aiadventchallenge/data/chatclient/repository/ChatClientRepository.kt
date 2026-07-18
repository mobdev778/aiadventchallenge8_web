package com.github.mobdev778.aiadventchallenge.data.chatclient.repository

import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.ChatRestApi
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.ChatRequest
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.ChatResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository
import retrofit2.Retrofit

@Repository
class ChatClientRepository(
    private val requestMapper: ChatRequestMapper,
    private val responseMapper: ChatResponseMapper,
    private val retrofit: Retrofit,
) {

    suspend fun sendRequest(
        request: ChatRequest,
    ): ChatResponse {
        println("ChatClientRepository.sendRequest")
        return withContext(Dispatchers.IO) {
            val restApi: ChatRestApi = retrofit.create(ChatRestApi::class.java)
            val requestDto = requestMapper.map(request)
            val responseDto = restApi.postChatCompletions(requestDto)
            responseMapper.map(responseDto)
        }
    }
}
