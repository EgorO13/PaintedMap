package com.paintedmap.domain.model

import kotlin.time.Duration

sealed interface NetworkError {
    data object Unauthorized : NetworkError
    data object NotFound : NetworkError
    data class Conflict(val reloadAllowed: Boolean) : NetworkError
    data class RateLimited(val retryAfter: Duration?) : NetworkError
    data object Unavailable : NetworkError
    data object InvalidData : NetworkError
}

sealed interface LoadResult<out T> {
    data class Success<T>(val value: T) : LoadResult<T>
    data class Failure(val error: NetworkError) : LoadResult<Nothing>
}

fun NetworkError.userMessage(): String = when (this) {
    NetworkError.Unauthorized -> "Войдите в аккаунт ещё раз"
    NetworkError.NotFound -> "Пользователь не найден"
    is NetworkError.Conflict -> "Данные уже изменились"
    is NetworkError.RateLimited -> "Слишком много запросов. Подождите"
    NetworkError.Unavailable -> "Не удалось загрузить данные. Попробуйте ещё раз"
    NetworkError.InvalidData -> "Не удалось прочитать данные сервера"
}