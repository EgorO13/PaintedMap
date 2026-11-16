package com.paintedmap.data

import com.paintedmap.data.remote.RemoteUserDataSource
import com.paintedmap.data.remote.UserApiService
import com.paintedmap.data.remote.network.NowTokenProvider
import com.paintedmap.data.remote.network.apiJson
import com.paintedmap.data.remote.network.createHttpClient
import com.paintedmap.data.repository.NetworkUserRepository
import com.paintedmap.domain.model.LoadResult
import com.paintedmap.domain.model.NetworkError
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class NetworkUserRepositoryTest {
    private lateinit var server: MockWebServer
    private lateinit var client: HttpClient
    private lateinit var repository: NetworkUserRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val baseUrl = Url(server.url("/").toString())
        val tokenProvider = NowTokenProvider("token")
        client = createHttpClient(
            baseUrl = baseUrl,
            tokens = tokenProvider
        )
        val apiService = UserApiService(client)
        val remoteDataSource = RemoteUserDataSource(
            api = apiService,
            maxGetRetries = 2,
            retryDelay = 10L
        )
        repository = NetworkUserRepository(remoteDataSource)
    }

    @After
    fun tearDown() {
        client.close()
        server.shutdown()
    }

    @Test
    fun testRequest() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content", "application/json")
                .setBody("""{"items":[],"next_cursor":null}""")
        )

        repository.searchUsers(query = "Bering", cursor = "cursor_2")

        val recorded = server.takeRequest()
        assertEquals("GET", recorded.method)
        assertEquals("/users", recorded.requestUrl?.encodedPath)
        assertEquals("Bering", recorded.requestUrl?.queryParameter("q"))
        assertEquals("cursor_2", recorded.requestUrl?.queryParameter("cursor"))
        assertEquals("20", recorded.requestUrl?.queryParameter("limit"))
        assertEquals("Bearer token", recorded.getHeader("Authorization"))
    }

    @Test
    fun test200() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content", "application/json")
                .setBody(
                    """
                    {
                      "items": [
                        {"id": 2, "nickname": "columbus", "name": "Chris_Columbus", "subs_count": 20, "authors_count": 20, "regions_count": 2}
                      ],
                      "next_cursor": "page_2"
                    }
                    """.trimIndent()
                )
        )
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content", "application/json")
                .setBody(
                    """
                    {
                      "items": [
                        {"id": 3, "nickname": "polo", "name": "Marco_Polo", "subs_count": 20, "authors_count": 20, "regions_count": 3}
                      ],
                      "next_cursor": null
                    }
                    """.trimIndent()
                )
        )

        val firstPage = repository.getSubscriptions(userId = 1, cursor = null)
        assertTrue(firstPage is LoadResult.Success)
        val firstValue = (firstPage as LoadResult.Success).value
        assertEquals(1, firstValue.items.size)
        assertEquals("Chris_Columbus", firstValue.items[0].userName)
        assertEquals("page_2", firstValue.nextCursor)

        val secondPage = repository.getSubscriptions(userId = 1, cursor = firstValue.nextCursor)
        assertTrue(secondPage is LoadResult.Success)
        val secondValue = (secondPage as LoadResult.Success).value
        assertEquals("Marco_Polo", secondValue.items[0].userName)
        assertNull(secondValue.nextCursor)
        server.takeRequest()
        val secondRequest = server.takeRequest()
        assertEquals("page_2", secondRequest.requestUrl?.queryParameter("cursor"))
    }

    @Test
    fun test401() = runTest {
        server.enqueue(MockResponse().setResponseCode(401))
        val result = repository.getUserById(1)
        assertEquals(LoadResult.Failure(NetworkError.Unauthorized), result)
    }

    @Test
    fun test404() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        val result = repository.getUserById(1000)
        assertEquals(LoadResult.Failure(NetworkError.NotFound), result)
    }

    @Test
    fun test409() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(409)
                .addHeader("Content", "application/json")
                .setBody("""{"code":"VERSION_CONFLICT"}""")
        )
        val result = repository.addUserRegion(userId = 1, regionId = "77")
        assertEquals(LoadResult.Failure(NetworkError.Conflict(reloadAllowed = true)), result)
    }

    @Test
    fun test429() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .addHeader("Retry-After", "5")
        )
        val result = repository.getUserById(1)
        assertEquals(
            LoadResult.Failure(NetworkError.RateLimited(retryAfter = 5.seconds)),
            result
        )
    }

    @Test
    fun test500() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))
        val result = repository.getUserById(1)
        assertEquals(LoadResult.Failure(NetworkError.Unavailable), result)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun testBrokenJson() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content", "application/json")
                .setBody("""{"id": 1, "broken_json": """)
        )
        val result = repository.getUserById(1)
        assertEquals(LoadResult.Failure(NetworkError.InvalidData), result)
    }

    @Test
    fun testInvalidData() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content", "application/json")
                .setBody("""{"id": 0, "nickname": "", "name": "Bad Name!"}""")
        )
        val result = repository.getUserById(1)
        assertEquals(LoadResult.Failure(NetworkError.InvalidData), result)
    }

    @Test
    fun test503Retry() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content", "application/json")
                .setBody("""{"id": 1, "nickname": "test", "name": "Test_User"}""")
        )

        val result = repository.getUserById(1)

        assertTrue(result is LoadResult.Success)
        assertEquals("Test_User", (result as LoadResult.Success).value.userName)
        assertEquals(2, server.requestCount)
    }

// task: delay in test

    @Test
    fun testTimeout() = runTest {
        val shortTimeoutClient = HttpClient(CIO) {
            expectSuccess = false
            defaultRequest {
                url { takeFrom(Url(server.url("/").toString())) }
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) { json(apiJson) }
            install(HttpTimeout) {
                connectTimeoutMillis = 200
                socketTimeoutMillis = 200
                requestTimeoutMillis = 200
            }
        }
        val shortRepo = NetworkUserRepository(
            RemoteUserDataSource(UserApiService(shortTimeoutClient), maxGetRetries = 0)
        )

        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content", "application/json")
                .setBodyDelay(1, TimeUnit.SECONDS)
                .setBody("""{"id": 1, "nickname": "test", "name": "Test_User"}""")
        )

        val result = shortRepo.getUserById(1)
        shortTimeoutClient.close()

        assertEquals(LoadResult.Failure(NetworkError.Unavailable), result)
    }

    @Test
    fun testCancel() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .addHeader("Content", "application/json")
                .setBodyDelay(2, TimeUnit.SECONDS)
                .setBody("""{"id": 1, "nickname": "test", "name": "Test_User"}""")
        )

        val deferred = async {
            repository.getUserById(1)
        }
        delay(50)
        deferred.cancel()

        val cancelled = runCatching { deferred.await() }.exceptionOrNull()
        assertTrue(cancelled is CancellationException)
    }
}
