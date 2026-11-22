package com.paintedmap.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse


// HttpResponse - хронит статус-код, служебную информацию и тело ответа в json
class UserApiService(private val client: HttpClient) {
    suspend fun getUserById(userId: Int): HttpResponse = client.get("users/$userId")
    suspend fun getSubscriptions(userId: Int, cursor: String? = null, limit: Int = 20, ): HttpResponse =
        client.get("users/$userId/subscriptions") {
            parameter("cursor", cursor)
            parameter("limit", limit)
        }
    suspend fun searchUsers(query: String, cursor: String? = null, limit: Int = 20, ): HttpResponse =
        client.get("users") {
            parameter("q", query)
            parameter("cursor", cursor)
            parameter("limit", limit)
        }
    suspend fun unsubscribe(followerId: Int, followingId: Int, ): HttpResponse =
        client.delete("users/$followerId/subscriptions/$followingId")

    suspend fun getUserRegions(userId: Int): HttpResponse =
        client.get("users/$userId/regions")

    suspend fun addUserRegion(userId: Int, regionId: String): HttpResponse =
        client.post("users/$userId/regions/$regionId")

    suspend fun removeUserRegion(userId: Int, regionId: String): HttpResponse =
        client.delete("users/$userId/regions/$regionId")
}