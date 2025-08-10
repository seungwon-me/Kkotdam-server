package me.seungwon.kkotdam.error.type

enum class ErrorCode(
    val status: Int,
    val type: String,
    val title: String,
    val detail: String
) {
    NO_RECOMMENDATION_FOUND(404, "/errors/no-recommendation-found", "No Recommendation Found", "입력하신 조건에 맞는 꽃 조합을 찾을 수 없습니다. 다른 조건으로 시도해 보세요."),
    FLOWER_NOT_FOUND(404, "/errors/flower-not-found", "Flower Not Found", "요청하신 꽃을 찾을 수 없습니다.")
}