package me.seungwon.kkotdam.error.type

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val message: String
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "Invalid Input Value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"),

    // Member
    EMAIL_DUPLICATION(HttpStatus.CONFLICT, "Email is Duplicated"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "Member not found"),

}
