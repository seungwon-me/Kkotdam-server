package me.seungwon.kkotdam.exception

import me.seungwon.kkotdam.error.type.ErrorCode

class KKotdamException(
    val errorCode: ErrorCode
) : RuntimeException()
