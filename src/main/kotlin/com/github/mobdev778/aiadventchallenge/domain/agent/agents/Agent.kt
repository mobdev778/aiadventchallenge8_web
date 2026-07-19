package com.github.mobdev778.aiadventchallenge.domain.agent.agents

import com.github.mobdev778.aiadventchallenge.domain.agent.AgentOrchestrator
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentContext
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentRequest
import com.github.mobdev778.aiadventchallenge.domain.agent.model.AgentResponse

abstract class Agent(
    val id: String,
) {

    abstract suspend fun handle(
        agentOrchestrator: AgentOrchestrator,
        context: AgentContext,
        request: AgentRequest,
    ): AgentResponse?

    override fun toString(): String {
        return "[$id] ${this.javaClass.simpleName}"
    }
}