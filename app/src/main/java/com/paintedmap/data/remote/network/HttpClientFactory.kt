package com.paintedmap.data.remote.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val apiJson: Json = Json { //Один экземпляр apiJson используется в Ktor Client и при чтении тела ошибки
    ignoreUnknownKeys = true //если сервер обновится и начнёт присылать лишние поля, приложение не упадёт, а просто проигнорирует их
    isLenient = false //включает строгий режим проверки синтаксиса JSON. Если сервер пришлёт кривой текст (пример: ключи без кавычек {id: 1}; лишние запятые), парсер сразу выдаст ошибку
    coerceInputValues = false //запрещает парсеру подменять неверные данные. Пример: если сервер пришлёт "subs_count": null в поле, которое не разрешает null, парсер не подставит туда значение по умолчанию, а сообщит, что пришёл некорректный ответ
}

fun interface TokenProvider { // при входе в аакаунт выдает токен
    fun currentToken(): String?
}


class NowTokenProvider( // потом токен будет храниться на сервере
    @Volatile private var token: String? = null
) : TokenProvider {
    override fun currentToken(): String? = token
    fun updateToken(newToken: String?) {
        token = newToken
    }
}

private class AuthConfig {
    // lateinit инициализация после создания, но до первого использования
    lateinit var baseUrl: Url
    lateinit var tokens: TokenProvider
}

// этот плагин перехватывает каждый исходящий запрос и сам приклеивает к нему токен и
// сверяет протокол, домен и порт. Если приложение обратится на сторонний сайт, токен туда не отправится, чтобы его не украли
private val ApiAuthPlugin = createClientPlugin("ApiAuth", ::AuthConfig) {
    val baseUrl = pluginConfig.baseUrl
    val tokens = pluginConfig.tokens

    onRequest { request, _ ->
        val token = tokens.currentToken()?.takeIf { it.isNotBlank() } ?: return@onRequest
        val sameOrigin = request.url.protocol == baseUrl.protocol &&
                request.url.host == baseUrl.host &&
                request.url.port == baseUrl.port
        if (sameOrigin && !request.headers.contains(HttpHeaders.Authorization)) {
            request.header(HttpHeaders.Authorization, "Bearer $token")
        }
    }
}

// собирает запрос на сервер
fun createHttpClient(
    baseUrl: Url,
    tokens: TokenProvider,
    engine: HttpClientEngineFactory<*> = CIO,
): HttpClient = HttpClient(engine) {
    expectSuccess = false // По умолчанию Ktor сам кидает общую ошибку при кодах 401, 404, 500, не давая прочитать тело ответа. Отключаем это поведение
    defaultRequest {
        url { takeFrom(baseUrl) }
        contentType(ContentType.Application.Json)
    }
    install(ContentNegotiation) { json(apiJson) }
    install(HttpTimeout) {
        connectTimeoutMillis = 5_000  // ожидание соединения
        socketTimeoutMillis = 10_000  // пауза между пакетами
        requestTimeoutMillis = 20_000 // ожидание всего ответа
    }
    install(Logging) { // делаем логи в логкэт
        logger = object : Logger {
            override fun log(message: String) {
                println("KtorHttp: $message")
            }
        }
        level = LogLevel.HEADERS
        sanitizeHeader { header -> header == HttpHeaders.Authorization } // заменяем токен на ***
    }
    install(ApiAuthPlugin) {
        this.baseUrl = baseUrl
        this.tokens = tokens
    }
}