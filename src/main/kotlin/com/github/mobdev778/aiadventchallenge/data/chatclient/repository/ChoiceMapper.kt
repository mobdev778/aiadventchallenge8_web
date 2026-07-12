package com.github.mobdev778.aiadventchallenge.data.chatclient.repository

import com.github.mobdev778.aiadventchallenge.data.chatclient.datasource.model.ChoiceDto
import com.github.mobdev778.aiadventchallenge.domain.chatclient.model.Choice
import org.springframework.stereotype.Component

@Component
class ChoiceMapper(
    private val finishReasonMapper: FinishReasonMapper,
    private val messageMapper: MessageMapper,
) {

    fun map(choiceDto: ChoiceDto): Choice {
        return Choice(
            message = messageMapper.map(choiceDto.message),
            finishReason = finishReasonMapper.map(choiceDto.finishReason),
        )
    }
}
