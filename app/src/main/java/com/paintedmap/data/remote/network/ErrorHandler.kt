package com.paintedmap.data.remote.network

import com.paintedmap.data.remote.dto.ApiErrorDTO
import com.paintedmap.data.remote.dto.ApiFailure
import com.paintedmap.domain.model.NetworkError
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.IOException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// переделываем ошибки от сервера в исключения
suspend fun httpFailure(response: HttpResponse): Nothing { //никогда не заканчивается успешно. цель: разобраться в причине сбоя и выбросить исключение ApiFailure
    val errorDto = decodeErrorBody(response)
    val networkError = when (response.status) {
        HttpStatusCode.Unauthorized -> NetworkError.Unauthorized // 401
        HttpStatusCode.NotFound -> NetworkError.NotFound // 404
        HttpStatusCode.Conflict -> NetworkError.Conflict( // 409
            reloadAllowed = errorDto?.code == "VERSION_CONFLICT" || errorDto?.code == null
        )
        HttpStatusCode.TooManyRequests -> NetworkError.RateLimited( // 429
            retryAfter = parseRetryAfter(response.headers[HttpHeaders.RetryAfter])
        )
        else -> NetworkError.Unavailable  // 50*
    }
    throw ApiFailure(response.status.value, networkError)
}

private suspend fun decodeErrorBody(response: HttpResponse): ApiErrorDTO? {
    return try {
        val text = response.bodyAsText()
        if (text.isBlank()) null
        else apiJson.decodeFromString<ApiErrorDTO>(text)
    } catch (error: Exception) {
        currentCoroutineContext().ensureActive()
        if (error is CancellationException) throw error
        null
    }
}

// смотрит из ответа сервера сколько надо подождать
private fun parseRetryAfter(headerValue: String?): Duration? {
    val seconds = headerValue?.trim()?.toLongOrNull() ?: return null
    return if (seconds > 0) seconds.seconds else null
}