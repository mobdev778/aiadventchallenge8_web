package com.github.mobdev778.aiadventchallenge.domain.chat.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ChatRepository : JpaRepository<ChatEntity, UUID>
