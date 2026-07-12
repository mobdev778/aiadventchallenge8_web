package com.github.mobdev778.aiadventchallenge.domain.chat.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "chats")
class ChatEntity(
    @Id
    @Column(nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val name: String = "",
    @Column(nullable = false)
    val time: Long = 0,
    @Column
    val parentId: UUID? = null,
)
