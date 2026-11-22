package com.paintedmap.domain.model

data class UserPage(
    val items: List<User>,
    val nextCursor: String? // Закладка для следующего запроса, если null то список закончился
)