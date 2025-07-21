package me.seungwon.kkotdam.exception

import me.seungwon.kkotdam.error.ErrorResponse
import me.seungwon.kkotdam.error.type.ErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(KKotdamException::class)
    fun handleKKotdamException(e: KKotdamException): ResponseEntity<ErrorResponse> {
        val errorCode = e.errorCode
        val errorResponse = ErrorResponse(errorCode.status.value(), errorCode.message)

        return ResponseEntity
            .status(errorCode.status)
            .body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.INVALID_INPUT_VALUE
        val errorMessage = e.bindingResult.allErrors.joinToString(", ") { it.defaultMessage ?: "Unknown error" }
        val errorResponse = ErrorResponse(errorCode.status.value(), errorMessage)

        return ResponseEntity
            .status(errorCode.status)
            .body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        val errorCode = ErrorCode.INTERNAL_SERVER_ERROR
        val errorResponse = ErrorResponse(errorCode.status.value(), e.message ?: errorCode.message)

        return ResponseEntity
            .status(errorCode.status)
            .body(errorResponse)
    }
}
