
package me.seungwon.kkotdam.flower

import me.seungwon.kkotdam.flower.dto.FlowerResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FlowerService(private val flowerRepository: FlowerRepository) {

    fun searchFlowers(search: String?): List<FlowerResponse> {
        val flowers = if (search.isNullOrBlank()) {
            flowerRepository.findAll()
        } else {
            flowerRepository.findByNameContaining(search)
        }
        return flowers.map { FlowerResponse(it.flowerId, it.name) }
    }
}
