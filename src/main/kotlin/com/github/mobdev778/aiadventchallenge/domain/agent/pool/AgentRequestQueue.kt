package com.github.mobdev778.aiadventchallenge.domain.agent.pool

import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentContext
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import kotlinx.coroutines.channels.Channel

class AgentRequestQueue {

    private val channel = Channel<Pair<AgentContext, AgentRequest>>(Channel.UNLIMITED)

    fun enqueue(context: AgentContext, message: AgentRequest) {
        channel.trySend(context to message)
    }

    suspend fun dequeue(): Pair<AgentContext, AgentRequest> {
        return channel.receive()
    }

    fun close() {
        channel.close()
    }
}
