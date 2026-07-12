package com.github.mobdev778.aiadventchallenge.domain.chatclient.model

data class ChatResponse(
    val choices: List<Choice>,
    val usage: Usage?,
)
