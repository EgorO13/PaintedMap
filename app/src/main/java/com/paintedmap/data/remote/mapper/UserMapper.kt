package com.paintedmap.data.remote.mapper

import com.paintedmap.data.remote.dto.UserDTO
import com.paintedmap.domain.model.User

class InvalidPayloadException(message: String) : Exception(message)

private val NAME_REGEX = Regex("^[A-Za-z0-9_]{3,20}$")

fun UserDTO.toDomain(): User {
    if (id <= 0) {
        throw InvalidPayloadException("Некорректный id пользователя: $id")
    }
    if (nickname.isBlank()) {
        throw InvalidPayloadException("Пустой nickname")
    }
    if (!name.matches(NAME_REGEX)) {
        throw InvalidPayloadException("Некорректное имя пользователя: $name")
    }

    val safeSubs = subsCount ?: 0
    val safeAuthors = authorsCount ?: 0
    val safeRegions = regionsCount ?: 0

    if (safeSubs < 0 || safeAuthors < 0 || safeRegions < 0) {
        throw InvalidPayloadException("Счётчики не могут быть отрицательными")
    }

    return User(
        userId = id,
        userNickName = nickname,
        userName = name,
        userSubsCount = safeSubs,
        userAuthorsCount = safeAuthors,
        userRegionsCount = safeRegions,
    )
}