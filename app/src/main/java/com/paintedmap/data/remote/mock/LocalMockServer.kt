package com.paintedmap.data.remote.mock

import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import java.util.concurrent.TimeUnit

enum class MockServerMode {
    NORMAL,
    CYCLE_ERRORS,
    ERROR_401,
    ERROR_404,
    ERROR_409,
    ERROR_429,
    ERROR_500,
    TIMEOUT,
    INVALID_JSON
}

class LocalMockServer {
    private val demoSequence = listOf(
        MockServerMode.NORMAL,
        MockServerMode.ERROR_401,
        MockServerMode.ERROR_404,
        MockServerMode.ERROR_409,
        MockServerMode.ERROR_429,
        MockServerMode.ERROR_500,
        MockServerMode.TIMEOUT,
        MockServerMode.INVALID_JSON
    )
    private var stepIndex = 0
    private val server = MockWebServer()

    @Volatile
    var mode: MockServerMode = MockServerMode.NORMAL

    private data class SeedUser(
        val id: Int,
        val nickname: String,
        val name: String,
        val following: MutableSet<Int>,
        val visitedRegions: MutableSet<String>
    )

    private val users = listOf(
        SeedUser(1, "test", "Test_User", (2..21).toMutableSet(), mutableSetOf("77", "78", "50")),
        SeedUser(2, "columbus", "Chris_Columbus", ((3..22)).toMutableSet(), mutableSetOf("39", "78")),
        SeedUser(3, "polo", "Marco_Polo", ((4..23)).toMutableSet(), mutableSetOf("16", "30", "08")),
        SeedUser(4, "magellan", "Ferd_Magellan", ((5..24)).toMutableSet(), mutableSetOf("65", "41")),
        SeedUser(5, "vasco", "Vasco_da_Gama", ((6..25)).toMutableSet(), mutableSetOf("23", "92")),
        SeedUser(6, "cook", "James_Cook", ((7..26)).toMutableSet(), mutableSetOf("41", "25", "65")),
        SeedUser(7, "bering", "Vitus_Bering", ((8..27)).toMutableSet(), mutableSetOf("41", "87", "14")),
        SeedUser(8, "amundsen", "Roald_Amundsen", ((9..28)).toMutableSet(), mutableSetOf("51", "29", "89")),
        SeedUser(9, "scott", "Robert_Scott", ((10..29)).toMutableSet(), mutableSetOf("51", "83")),
        SeedUser(10, "shackleton", "E_Shackleton", ((11..30)).toMutableSet(), mutableSetOf("51", "39")),
        SeedUser(11, "nansen", "Fridtjof_Nansen", (listOf(1) + (12..30)).toMutableSet(), mutableSetOf("51", "29", "24")),
        SeedUser(12, "livingstone", "D_Livingstone", ((1..2) + (13..30)).toMutableSet(), mutableSetOf("26", "07")),
        SeedUser(13, "stanley", "Henry_Stanley", ((1..3) + (14..30)).toMutableSet(), mutableSetOf("34", "61")),
        SeedUser(14, "drake", "Francis_Drake", ((1..4) + (15..30)).toMutableSet(), mutableSetOf("39", "78")),
        SeedUser(15, "vespucci", "Amerigo_Vespucci", ((1..5) + (16..30)).toMutableSet(), mutableSetOf("77", "91")),
        SeedUser(16, "cortes", "Hernan_Cortes", ((1..6) + (17..30)).toMutableSet(), mutableSetOf("23", "01")),
        SeedUser(17, "pizarro", "F_Pizarro", ((1..7) + (18..30)).toMutableSet(), mutableSetOf("04", "22")),
        SeedUser(18, "cabot", "John_Cabot", ((1..8) + (19..30)).toMutableSet(), mutableSetOf("29", "10")),
        SeedUser(19, "hudson", "Henry_Hudson", ((1..9) + (20..30)).toMutableSet(), mutableSetOf("51", "83", "11")),
        SeedUser(20, "tasman", "Abel_Tasman", ((1..10) + (21..30)).toMutableSet(), mutableSetOf("25", "65")),
        SeedUser(21, "ibn_battuta", "Ibn_Battuta", ((1..11) + (22..30)).toMutableSet(), mutableSetOf("05", "30", "16")),
        SeedUser(22, "zheng_he", "Zheng_He", ((1..12) + (23..30)).toMutableSet(), mutableSetOf("25", "28", "79")),
        SeedUser(23, "przhevalsky", "N_Przhevalsky", ((1..13) + (24..30)).toMutableSet(), mutableSetOf("67", "03", "75")),
        SeedUser(24, "dezhnev", "Semyon_Dezhnev", ((1..14) + (25..30)).toMutableSet(), mutableSetOf("14", "87", "49")),
        SeedUser(25, "ermak", "Ermak_Timofeevich", ((1..15) + (26..30)).toMutableSet(), mutableSetOf("72", "66", "59")),
        SeedUser(26, "kruzenshtern", "I_Kruzenshtern", ((1..16) + (27..30)).toMutableSet(), mutableSetOf("78", "47", "41")),
        SeedUser(27, "bellingshausen", "F_Bellingshausen", ((1..17) + (28..30)).toMutableSet(), mutableSetOf("78", "92", "39")),
        SeedUser(28, "nikitin", "Afanasy_Nikitin", ((1..18) + (29..30)).toMutableSet(), mutableSetOf("69", "30", "05")),
        SeedUser(29, "konukhov", "Fedor_Konyukhov", ((1..19) + listOf(30)).toMutableSet(), mutableSetOf("25", "74", "77")),
        SeedUser(30, "humboldt", "Alex_Humboldt", (1..20).toMutableSet(), mutableSetOf("66", "72", "22"))
    )

    init {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val p = request.requestUrl?.encodedPath.orEmpty()
                if (mode == MockServerMode.CYCLE_ERRORS && p.matches(Regex("^/users/\\d+$"))) {
                    stepIndex = (stepIndex + 1) % demoSequence.size
                }
                val activeMode = if (mode == MockServerMode.CYCLE_ERRORS) {
                    demoSequence[stepIndex]
                } else {
                    mode
                }
                when (activeMode) {
                    MockServerMode.CYCLE_ERRORS -> Unit
                    MockServerMode.ERROR_401 -> return MockResponse().setResponseCode(401)
                    MockServerMode.ERROR_404 -> return MockResponse().setResponseCode(404)
                    MockServerMode.ERROR_409 -> return MockResponse()
                        .setResponseCode(409)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""{"code":"VERSION_CONFLICT"}""")
                    MockServerMode.ERROR_429 -> return MockResponse()
                        .setResponseCode(429)
                        .addHeader("Retry-After", "5")
                    MockServerMode.ERROR_500 -> return MockResponse().setResponseCode(500)
                    MockServerMode.TIMEOUT -> return MockResponse()
                        .setSocketPolicy(SocketPolicy.NO_RESPONSE)
                    MockServerMode.INVALID_JSON -> return MockResponse()
                        .setResponseCode(200)
                        .addHeader("Content-Type", "application/json")
                        .setBody("""{"broken_json": """)
                    MockServerMode.NORMAL -> Unit
                }

                val url = request.requestUrl ?: return MockResponse().setResponseCode(404)
                val path = url.encodedPath

                if (path.matches(Regex("^/users/\\d+$")) && request.method == "GET") {
                    val userId = path.substringAfterLast("/").toIntOrNull()
                    val user = users.find { it.id == userId }
                        ?: return MockResponse().setResponseCode(404)

                    return jsonResponse(userToJson(user))
                }

                if (path.matches(Regex("^/users/\\d+/subscriptions$")) && request.method == "GET") {
                    val userId = path.removePrefix("/users/").substringBefore("/").toIntOrNull()
                    val user = users.find { it.id == userId }
                        ?: return MockResponse().setResponseCode(404)

                    val subUsers = users.filter { it.id in user.following }
                    val cursor = url.queryParameter("cursor")?.toIntOrNull() ?: 0
                    return jsonResponse(buildPageJson(subUsers, cursor, pageSize = 8))
                }

                if (path == "/users" && request.method == "GET") {
                    val query = url.queryParameter("q").orEmpty()
                    val found = users.filter {
                        it.name.contains(query, ignoreCase = true) ||
                                it.nickname.contains(query, ignoreCase = true)
                    }
                    val cursor = url.queryParameter("cursor")?.toIntOrNull() ?: 0
                    return jsonResponse(buildPageJson(found, cursor, pageSize = 8))
                }

                if (path.matches(Regex("^/users/\\d+/subscriptions/\\d+$")) && request.method == "DELETE") {
                    val parts = path.split("/")
                    val followerId = parts.getOrNull(2)?.toIntOrNull()
                    val followingId = parts.getOrNull(4)?.toIntOrNull()
                    users.find { it.id == followerId }?.following?.remove(followingId)
                    return MockResponse().setResponseCode(204)
                }

                if (path.matches(Regex("^/users/\\d+/regions$")) && request.method == "GET") {
                    val userId = path.removePrefix("/users/").substringBefore("/").toIntOrNull()
                    val user = users.find { it.id == userId }
                        ?: return MockResponse().setResponseCode(404)

                    val regionsJson = user.visitedRegions.joinToString(",") { "\"$it\"" }
                    return jsonResponse("""{"items":[$regionsJson]}""")
                }

                if (path.matches(Regex("^/users/\\d+/regions/[A-Za-z0-9_-]+$")) && request.method == "POST") {
                    val parts = path.split("/")
                    val userId = parts.getOrNull(2)?.toIntOrNull()
                    val regionId = parts.getOrNull(4)
                    val user = users.find { it.id == userId }
                        ?: return MockResponse().setResponseCode(404)

                    if (!regionId.isNullOrBlank()) {
                        user.visitedRegions.add(regionId)
                    }
                    return MockResponse().setResponseCode(204)
                }

                if (path.matches(Regex("^/users/\\d+/regions/[A-Za-z0-9_-]+$")) && request.method == "DELETE") {
                    val parts = path.split("/")
                    val userId = parts.getOrNull(2)?.toIntOrNull()
                    val regionId = parts.getOrNull(4)
                    val user = users.find { it.id == userId }
                        ?: return MockResponse().setResponseCode(404)

                    if (!regionId.isNullOrBlank()) {
                        user.visitedRegions.remove(regionId)
                    }
                    return MockResponse().setResponseCode(204)
                }

                return MockResponse().setResponseCode(404)
            }
        }
    }

    suspend fun startAndGetUrl(): Url = withContext(Dispatchers.IO) {
        server.start()
        Url(server.url("/").toString())
    }

    suspend fun stop() = withContext(Dispatchers.IO) {
        runCatching { server.shutdown() }
    }

    private fun buildPageJson(list: List<SeedUser>, offset: Int, pageSize: Int): String {
        val slice = list.drop(offset).take(pageSize)
        val nextOffset = offset + pageSize
        val nextCursorJson = if (nextOffset < list.size) "\"$nextOffset\"" else "null"
        val itemsJson = slice.joinToString(",") { userToJson(it) }
        return """{"items":[$itemsJson],"next_cursor":$nextCursorJson}"""
    }

    private fun userToJson(u: SeedUser): String {
        val followersCount = users.count { u.id in it.following }
        return """{"id":${u.id},"nickname":"${u.nickname}","name":"${u.name}","subs_count":$followersCount,"authors_count":${u.following.size},"regions_count":${u.visitedRegions.size}}"""
    }
    private fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .addHeader("Content-Type", "application/json")
            .setBodyDelay(350, TimeUnit.MILLISECONDS)
            .setBody(body)
}