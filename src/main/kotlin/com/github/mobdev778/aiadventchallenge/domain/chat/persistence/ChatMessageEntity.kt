package com.github.mobdev778.aiadventchallenge.domain.chat.persistence

import com.github.mobdev778.aiadventchallenge.domain.chat.model.MessageType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "chat_messages")
class ChatMessageEntity(
    @Id
    @Column(nullable = false, updatable = false)
    val id: UUID = UUID.randomUUID(),
    @Column(nullable = false)
    val chatId: UUID = UUID.randomUUID(),
    @Column
    val parentId: UUID? = null,
    @Column(nullable = false)
    val time: Long = 0,
    @Column(nullable = false, columnDefinition = "TEXT")
    val text: String = "",
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MessageType = MessageType.User,
    @Column(nullable = false)
    val tokens: Int = 0,
    @Column
    val toolCallId: String? = null,
    @Column
    val name: String? = null,
)
