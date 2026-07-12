package com.github.mobdev778.aiadventchallenge.domain.agent.agents

import com.github.mobdev778.aiadventchallenge.domain.profile.model.Profile
import java.util.UUID

class SystemPromptBuilder {

    private var chatId: UUID? = null
    private var query: String = ""
    private lateinit var profile: Profile

    fun chatId(chatId: UUID) = apply {
        this.chatId = chatId
    }

    fun query(query: String) = apply {
        this.query = query
    }

    fun profile(profile: Profile) = apply {
        this.profile = profile
    }

    fun build(): String {
        return """
            chatId: \"$chatId\"
            [PROFILE] ${profile.content}
        """.trimIndent()
    }
}
