package com.github.mobdev778.aiadventchallenge.domain.agent.agents.crm.context

import io.ktor.util.collections.ConcurrentMap
import java.util.UUID

object CrmContextRegistry {

    private val idToContextMap = ConcurrentMap<UUID, CrmContext>()

    fun get(chatId: UUID): CrmContext? {
        return idToContextMap[chatId]
    }

    fun put(chatId: UUID, context: CrmContext) {
        idToContextMap[chatId] = context
    }
}