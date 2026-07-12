package com.github.mobdev778.aiadventchallenge.data.chatclient.repository

import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.ReasoningEffortDto
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.ReasoningEffort
import org.springframework.stereotype.Component

@Component
class ReasoningEffortMapper {

    fun map(reasoningEffort: ReasoningEffort): ReasoningEffortDto {
        return when (reasoningEffort) {
            ReasoningEffort.High -> ReasoningEffortDto.High
            ReasoningEffort.Medium -> ReasoningEffortDto.Medium
            ReasoningEffort.Low -> ReasoningEffortDto.Low
        }
    }
}
