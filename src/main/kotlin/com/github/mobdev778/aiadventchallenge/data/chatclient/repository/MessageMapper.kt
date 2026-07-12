package com.github.mobdev778.aiadventchallenge.data.chatclient.repository

import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.MessageDto
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.Message
import org.springframework.stereotype.Component

@Component
class MessageMapper(
    private val roleMapper: RoleMapper,
    private val toolCallMapper: ToolCallMapper,
) {

    fun map(message: Message): MessageDto {
        return MessageDto(
            role = roleMapper.map(message.role),
            content = message.content,
            // доп параметры MCP:
            name = message.name,
            toolCallId = message.toolCallId,
            toolCalls = message.toolCalls?.map {
                toolCallMapper.map(it)
            },
        )
    }

    fun map(messageDto: MessageDto): Message {
        return Message(
            role = roleMapper.map(messageDto.role),
            content = messageDto.content,
            // доп.параметры MCP:
            name = messageDto.name,
            toolCallId = messageDto.toolCallId,
            toolCalls = messageDto.toolCalls?.map {
                toolCallMapper.map(it)
            },
        )
    }
}
