package me.seungwon.kkotdam.error

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.servlet.http.HttpServletRequest
import me.seungwon.kkotdam.error.type.ErrorCode
import org.springframework.http.ResponseEntity
import java.net.URI

/*
{
  "type": "https://example.com/errors/invalid-parameters", // 에러 종류를 식별하는 URI
  "title": "Invalid Parameters", // 사람이 읽을 수 있는 에러 요약
  "status": 400, // HTTP 상태 코드
  "detail": "occasion 필드는 필수입니다.", // 에러에 대한 구체적인 설명
  "instance": "/recommendations" // 에러가 발생한 요청 경로
}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val type: URI,
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String
) {

    companion object {

        // static factory method
        fun of(errorCode: ErrorCode, request: HttpServletRequest, detail: String? = null): ResponseEntity<ErrorResponse> {
            return ResponseEntity
                .status(errorCode.status)
                .body(
                    ErrorResponse(
                        type = URI.create("/errors/${errorCode.name}"),
                        title = errorCode.message,
                        status = errorCode.status.value(),
                        detail = detail ?: errorCode.message,
                        instance = request.requestURI
                    )
                )
        }
    }
}
