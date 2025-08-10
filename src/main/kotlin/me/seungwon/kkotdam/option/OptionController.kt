
package me.seungwon.kkotdam.option

import me.seungwon.kkotdam.option.dto.Option
import me.seungwon.kkotdam.option.dto.OptionResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/options")
class OptionController {

    @GetMapping
    fun getOptions(): OptionResponse {
        return OptionResponse(
            occasions = listOf(
                Option("LOVE", "사랑"),
                Option("THANKS", "감사"),
                Option("BIRTHDAY", "생일")
            ),
            recipients = listOf(
                Option("PARTNER", "연인"),
                Option("PARENTS", "부모님"),
                Option("FRIEND", "친구")
            ),
            moods = listOf(
                Option("BRIGHT", "화사하게"),
                Option("CALM", "차분하게"),
                Option("LUXURIOUS", "고급스럽게")
            ),
            sizes = listOf(
                Option("S", "Small"),
                Option("M", "Medium"),
                Option("L", "Large")
            )
        )
    }
}
