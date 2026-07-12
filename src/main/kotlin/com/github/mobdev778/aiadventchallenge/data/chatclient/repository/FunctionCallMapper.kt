package com.github.mobdev778.aiadventchallenge.data.chatclient.repository

import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.FunctionCallDto
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.FunctionCall
import org.springframework.stereotype.Component

@Component
class FunctionCallMapper {

    fun map(functionCall: FunctionCall): FunctionCallDto {
        return FunctionCallDto(
            name = functionCall.name,
            arguments = functionCall.arguments,
        )
    }

    fun map(dto: FunctionCallDto): FunctionCall {
        return FunctionCall(
            name = dto.name,
            arguments = dto.arguments,
        )
    }
}
