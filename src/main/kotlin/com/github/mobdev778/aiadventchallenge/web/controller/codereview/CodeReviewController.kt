package com.github.mobdev778.aiadventchallenge.web.controller.codereview

import com.github.mobdev778.aiadventchallenge.domain.codereview.CodeReviewService
import com.github.mobdev778.aiadventchallenge.domain.codereview.model.CodeReviewRequest
import com.github.mobdev778.aiadventchallenge.domain.codereview.model.CodeReviewResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CodeReviewController(
    private val codeReviewService: CodeReviewService,
) {

    @PostMapping("/codereview")
    suspend fun codeReview(
        @RequestBody request: CodeReviewRequest,
    ): CodeReviewResponse {
        println("!!! CodeReviewController: source: ${request.source}, target: ${request.target}")
        try {
            return codeReviewService.postCodeReviewRequest(request)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
