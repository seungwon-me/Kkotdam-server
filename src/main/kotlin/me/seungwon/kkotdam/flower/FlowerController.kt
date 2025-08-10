
package me.seungwon.kkotdam.flower

import me.seungwon.kkotdam.flower.dto.FlowerResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/flowers")
class FlowerController(private val flowerService: FlowerService) {

    @GetMapping
    fun getFlowers(@RequestParam(required = false) search: String?): List<FlowerResponse> {
        return flowerService.searchFlowers(search)
    }
}
