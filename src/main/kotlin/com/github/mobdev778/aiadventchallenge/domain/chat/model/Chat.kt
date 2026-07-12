package com.github.mobdev778.aiadventchallenge.domain.chat.model

import java.util.UUID

data class Chat(
    val id: UUID,
    val name: String,
    val time: Long,
    val parentId: UUID?,
)
