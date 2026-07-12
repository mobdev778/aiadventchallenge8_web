package com.github.mobdev778.aiadventchallenge.domain.chat.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ChatMessageRepository : JpaRepository<ChatMessageEntity, UUID> {
    fun findAllByChatIdOrderByTimeAsc(chatId: UUID): List<ChatMessageEntity>
}
