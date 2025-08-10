package me.seungwon.kkotdam.error

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
    @get:JsonProperty("instance") // 'instance' is a reserved keyword in Kotlin
    val requestPath: String
)
