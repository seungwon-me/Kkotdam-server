package me.seungwon.kkotdam.exception

import me.seungwon.kkotdam.error.type.ErrorCode

class KKotdamException(
    val errorCode: ErrorCode,
    override val message: String? = null
) : RuntimeException(message ?: errorCode.detail)