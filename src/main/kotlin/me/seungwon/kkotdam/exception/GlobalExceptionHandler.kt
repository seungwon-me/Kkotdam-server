package me.seungwon.kkotdam.exception

import jakarta.servlet.http.HttpServletRequest
import me.seungwon.kkotdam.error.ErrorResponse
import me.seungwon.kkotdam.error.type.ErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(KKotdamException::class)
    fun handleKKotdamException(e: KKotdamException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ErrorResponse.of(e.errorCode, request, e.message)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val errorMessage = e.bindingResult.allErrors.joinToString(", ") { it.defaultMessage ?: "Unknown error" }
        return ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, request, errorMessage)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        return ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, request, e.message)
    }
}
