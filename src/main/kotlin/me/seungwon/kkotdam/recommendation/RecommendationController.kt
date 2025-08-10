
package me.seungwon.kkotdam.recommendation

import jakarta.validation.Valid
import me.seungwon.kkotdam.recommendation.dto.RecommendationRequest
import me.seungwon.kkotdam.recommendation.dto.RecommendationResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/recommendations")
class RecommendationController(private val recommendationService: RecommendationService) {

    @PostMapping
    fun recommend(@Valid @RequestBody request: RecommendationRequest): RecommendationResponse {
        return recommendationService.recommend(request)
    }
}
