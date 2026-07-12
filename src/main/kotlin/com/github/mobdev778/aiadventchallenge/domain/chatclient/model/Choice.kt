package com.github.mobdev778.aiadventchallenge.domain.chatclient.model

data class Choice(
    val message: Message,
    val finishReason: FinishReason,
)
