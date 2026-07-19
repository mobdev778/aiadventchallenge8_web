package com.github.mobdev778.aiadventchallenge.domain.agent.model

enum class AgentType {
    ChatAssistant, // простой диалог
    Help,          // помощь (информация) по проекту
    CodeReview,    // код ревью
    CrmStart,      // старт общения с CRM-ассистентом
    CrmChat,       // чат с CRM-ассистентом
}