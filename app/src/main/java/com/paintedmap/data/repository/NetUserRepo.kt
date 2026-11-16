package com.paintedmap.data.repository

import com.paintedmap.data.remote.RemoteUserDataSource
import com.paintedmap.data.remote.dto.ApiFailure
import com.paintedmap.data.remote.mapper.InvalidPayloadException
import com.paintedmap.data.remote.mapper.toDomain
import com.paintedmap.domain.model.LoadResult
import com.paintedmap.domain.model.NetworkError
import com.paintedmap.domain.model.User
import com.paintedmap.domain.model.UserPage
import com.paintedmap.domain.repository.UserRepository
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.serialization.ContentConvertException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import java.io.IOException

class NetworkUserRepository(
    private val remote: RemoteUserDataSource,
) : UserRepository {

    override suspend fun getUserById(userId: Int): LoadResult<User> = safeCall {
        remote.getUserById(userId).toDomain()
    }

    override suspend fun getSubscriptions(
        userId: Int,
        cursor: String?,
    ): LoadResult<UserPage> = safeCall {
        remote.getSubscriptions(userId, cursor).toDomain()
    }

    override suspend fun searchUsers(query: String, cursor: String?, ): LoadResult<UserPage> = safeCall {
        remote.searchUsers(query, cursor).toDomain()
    }

    override suspend fun unsubscribe(
        followerId: Int,
        followingId: Int,
    ): LoadResult<Unit> = safeCall {
        remote.unsubscribe(followerId, followingId)
    }

    override suspend fun getUserRegions(userId: Int): LoadResult<Set<String>> = safeCall {
        remote.getUserRegions(userId)
    }

    override suspend fun addUserRegion(userId: Int, regionId: String): LoadResult<Unit> = safeCall {
        remote.addUserRegion(userId, regionId)
    }

    override suspend fun removeUserRegion(userId: Int, regionId: String): LoadResult<Unit> = safeCall {
        remote.removeUserRegion(userId, regionId)
    }

    private suspend inline fun <T> safeCall(crossinline block: suspend () -> T): LoadResult<T> {
        return try {
            LoadResult.Success(block())
        } catch (error: Exception) {
            currentCoroutineContext().ensureActive()
            val failure = when (error) {
                is CancellationException -> throw error
                is ApiFailure -> error.error
                is IOException -> NetworkError.Unavailable
                is SerializationException,
                is ContentConvertException,
                is NoTransformationFoundException,
                is InvalidPayloadException -> NetworkError.InvalidData
                else -> throw error
            }
            LoadResult.Failure(failure)
        }
    }
}
