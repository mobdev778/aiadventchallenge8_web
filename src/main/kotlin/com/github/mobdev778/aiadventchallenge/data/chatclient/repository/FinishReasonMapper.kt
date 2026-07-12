package com.github.mobdev778.aiadventchallenge.data.chatclient.repository

import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.FinishReasonDto
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.FinishReason
import org.springframework.stereotype.Component

@Component
class FinishReasonMapper {

    fun map(reason: FinishReasonDto): FinishReason {
        return when (reason) {
            FinishReasonDto.Stop -> FinishReason.Stop
            FinishReasonDto.Length -> FinishReason.Length
            FinishReasonDto.ToolCalls -> FinishReason.ToolCalls
            FinishReasonDto.ContentFilter -> FinishReason.ContentFilter
            FinishReasonDto.RleMaxTokens -> FinishReason.RleMaxTokens
        }
    }
}
