package com.github.mobdev778.aiadventchallenge.data.chatclient.datasource

import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.ChatRequestDto
import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.ChatResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatRestApi {

    @POST("chat/completions")
    suspend fun postChatCompletions(
        @Body request: ChatRequestDto
    ): ChatResponseDto
}
