package com.paintedmap.data.remote

import com.paintedmap.data.remote.dto.UserDTO
import com.paintedmap.data.remote.dto.UserPageDTO
import com.paintedmap.data.remote.dto.UserRegionsDTO
import com.paintedmap.data.remote.network.httpFailure
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import java.io.IOException

class RemoteUserDataSource(
    private val api: UserApiService,
    private val maxGetRetries: Int = 2,
    private val retryDelay: Long = 300L,
) {
    private companion object {
        val RETRYABLE_STATUS_CODES = setOf(502, 503, 504)
    }

    suspend fun getUserById(userId: Int): UserDTO = executeGet {
        api.getUserById(userId)
    }

    suspend fun getSubscriptions(userId: Int, cursor: String?): UserPageDTO = executeGet {
        api.getSubscriptions(userId = userId, cursor = cursor)
    }

    suspend fun searchUsers(query: String, cursor: String?): UserPageDTO = executeGet {
        api.searchUsers(query = query, cursor = cursor)
    }

    suspend fun unsubscribe(followerId: Int, followingId: Int) {
        val response = api.unsubscribe(followerId = followerId, followingId = followingId)
        if (!response.status.isSuccess()) {
            httpFailure(response)
        }
    }

    suspend fun getUserRegions(userId: Int): Set<String> = executeGet<UserRegionsDTO> {
        api.getUserRegions(userId)
    }.items.toSet()

    suspend fun addUserRegion(userId: Int, regionId: String) {
        val response = api.addUserRegion(userId, regionId)
        if (!response.status.isSuccess()) {
            httpFailure(response)
        }
    }

    suspend fun removeUserRegion(userId: Int, regionId: String) {
        val response = api.removeUserRegion(userId, regionId)
        if (!response.status.isSuccess()) {
            httpFailure(response)
        }
    }

    private suspend inline fun <reified T> executeGet(
        request: suspend () -> HttpResponse
    ): T {
        for (attempt in 0..maxGetRetries) {
            try {
                val response = request()
                if (response.status.isSuccess()) {
                    return response.body()
                }
                if (response.status.value !in RETRYABLE_STATUS_CODES || attempt == maxGetRetries) {
                    httpFailure(response)
                }
            } catch (e: IOException) {
                if (attempt == maxGetRetries) throw e
            }
            delay(retryDelay * (attempt + 1))
        }
        error("Unreachable")
    }
}