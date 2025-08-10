
package me.seungwon.kkotdam.recommendation

import me.seungwon.kkotdam.error.type.ErrorCode
import me.seungwon.kkotdam.exception.KKotdamException
import me.seungwon.kkotdam.flower.Flower
import me.seungwon.kkotdam.flower.FlowerRepository
import me.seungwon.kkotdam.recommendation.dto.FlowerInfo
import me.seungwon.kkotdam.recommendation.dto.RecommendationRequest
import me.seungwon.kkotdam.recommendation.dto.RecommendationResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class RecommendationService(private val flowerRepository: FlowerRepository) {

    fun recommend(request: RecommendationRequest): RecommendationResponse {
        val allFlowers = flowerRepository.findAll()

        val includedFlowers = request.includeFlowers?.mapNotNull { flowerId ->
            allFlowers.find { it.flowerId == flowerId }
        } ?: emptyList()

        val availableFlowers = allFlowers
            .filter { it.flowerId !in (request.excludeFlowers ?: emptyList()) }
            .filter { it.flowerId !in (request.includeFlowers ?: emptyList()) }

        val recommendedFlowers = mutableListOf<Flower>()
        recommendedFlowers.addAll(includedFlowers)

        // Simple rule: Add 2 more random flowers if the list is not full
        if (recommendedFlowers.size < 3) {
            recommendedFlowers.addAll(availableFlowers.shuffled().take(3 - recommendedFlowers.size))
        }

        if (recommendedFlowers.isEmpty()) {
            throw KKotdamException(ErrorCode.NO_RECOMMENDATION_FOUND)
        }

        val firstFlower = recommendedFlowers.first()

        return RecommendationResponse(
            combinationId = "combo_${recommendedFlowers.joinToString("_") { it.flowerId }}",
            combinationName = "${firstFlower.name}을(를) 위한 조합",
            description = "${request.occasion}을(를) 위한 특별한 꽃 조합입니다.",
            combinationImageUrl = firstFlower.imageUrl,
            flowers = recommendedFlowers.map { flower ->
                FlowerInfo(
                    flowerId = flower.flowerId,
                    name = flower.name,
                    imageUrl = flower.imageUrl,
                    meaning = flower.meaning
                )
            }
        )
    }
}
