package com.github.mobdev778.aiadventchallenge.domain.mcp.model

import java.nio.charset.StandardCharsets
import java.util.UUID

data class McpServer(
    val id: UUID,
    val name: String,
    val url: String,
) {
    constructor(name: String, url: String) : this(
        id = UUID.nameUUIDFromBytes(url.toByteArray(StandardCharsets.UTF_8)),
        name = name,
        url = url,
    )
}
