package com.github.mobdev778.aiadventchallenge.web.controller.codereview

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.Ignore

// @Ignore
class CodeReviewRemoteControllerTest {

    @Serializable
    data class Response(val status: String, val message: String)

    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `POST codereview should return success with empty message`() {
        val requestBody = """{"source":"day_32_fix","target":"day_32"}"""

        val request = Request.Builder()
            .url("http://150.241.79.134:8080/codereview")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            assertEquals(200, response.code)
            val body = response.body?.string() ?: "{}"
            val result = json.decodeFromString<Response>(body)
            println("result: $result")
            assertEquals("success", result.status)
        }
    }
}