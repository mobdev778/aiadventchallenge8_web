package com.github.mobdev778.aiadventchallenge.data.chatclient.repository

import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.ToolCallDto
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.ToolCall
import org.springframework.stereotype.Component

@Component
class ToolCallMapper(
    private val functionCallMapper: FunctionCallMapper,
) {

    fun map(toolCall: ToolCall): ToolCallDto {
        return ToolCallDto(
            id = toolCall.id,
            type = toolCall.type,
            function = functionCallMapper.map(toolCall.function),
        )
    }

    fun map(dto: ToolCallDto): ToolCall {
        return ToolCall(
            id = dto.id,
            type = dto.type,
            function = functionCallMapper.map(dto.function),
        )
    }
}
