
package me.seungwon.kkotdam.recommendation.dto

data class RecommendationResponse(
    val combinationId: String,
    val combinationName: String,
    val description: String,
    val combinationImageUrl: String,
    val flowers: List<FlowerInfo>
)

data class FlowerInfo(
    val flowerId: String,
    val name: String,
    val imageUrl: String,
    val meaning: String
)
