
package me.seungwon.kkotdam.recommendation.dto

import jakarta.validation.constraints.NotEmpty

data class RecommendationRequest(
    @field:NotEmpty
    val occasion: String,
    @field:NotEmpty
    val recipient: String,
    @field:NotEmpty
    val mood: String,
    val includeFlowers: List<String>? = emptyList(),
    val excludeFlowers: List<String>? = emptyList(),
    @field:NotEmpty
    val size: String
)
