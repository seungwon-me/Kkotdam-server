package me.seungwon.kkotdam.exception

import me.seungwon.kkotdam.error.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import jakarta.servlet.http.HttpServletRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val detail = ex.bindingResult.allErrors.firstOrNull()?.defaultMessage ?: "Validation failed"
        val errorResponse = ErrorResponse(
            type = "/errors/invalid-parameters",
            title = "Invalid Parameters",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = detail,
            requestPath = request.requestURI
        )
        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            type = "/errors/internal-server-error",
            title = "Internal Server Error",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            detail = ex.message ?: "An unexpected error occurred",
            requestPath = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    @ExceptionHandler(KKotdamException::class)
    fun handleKKotdamException(
        ex: KKotdamException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            type = ex.errorCode.type,
            title = ex.errorCode.title,
            status = ex.errorCode.status,
            detail = ex.message ?: ex.errorCode.detail,
            requestPath = request.requestURI
        )
        return ResponseEntity.status(ex.errorCode.status).body(errorResponse)
    }
}