package com.github.mobdev778.aiadventchallenge.data.chatclient.repository

import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.ChatRequestDto
import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.FunctionDto
import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.ToolDto
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.ChatRequest
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import org.springframework.stereotype.Component

@Component
class ChatRequestMapper(
    private val reasoningEffortMapper: ReasoningEffortMapper,
    private val messageMapper: MessageMapper,
) {

    fun map(request: ChatRequest): ChatRequestDto {
        val tools = request.tools
            .takeIf { it.isNotEmpty() }
            ?.map(::map)

        return ChatRequestDto(
            model = request.model,
            reasoningEffort = request.reasoningEffort?.let {
                reasoningEffortMapper.map(it)
            },
            messages = request.messages.map { message ->
                messageMapper.map(message)
            },
            maxTokens = request.maxTokens,
            tools = tools,
            toolChoice = tools?.let { "auto" },
            temperature = request.temperature
        )
    }

    private fun map(tool: Tool): ToolDto {
        val parametersJson = buildJsonObject {
            put("type", JsonPrimitive("object"))

            val props = tool.inputSchema.properties
            if (props != null) {
                put("properties", props)
            }

            val requiredList = tool.inputSchema.required
            if (requiredList?.isNotEmpty() == true) {
                putJsonArray("required") {
                    requiredList.forEach { add(JsonPrimitive(it)) }
                }
            }
        }

        return ToolDto(
            type = "function",
            function = FunctionDto(
                name = tool.name,
                description = tool.description,
                parameters = parametersJson,
            )
        )
    }
}
