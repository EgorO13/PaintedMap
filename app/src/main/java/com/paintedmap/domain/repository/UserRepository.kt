package com.paintedmap.domain.repository

import com.paintedmap.domain.model.LoadResult
import com.paintedmap.domain.model.User
import com.paintedmap.domain.model.UserPage

interface UserRepository {
    suspend fun getUserById(userId: Int): LoadResult<User>

    suspend fun getSubscriptions(userId: Int, cursor: String? = null, ): LoadResult<UserPage>

    suspend fun searchUsers(query: String, cursor: String? = null, ): LoadResult<UserPage>

    suspend fun unsubscribe(followerId: Int, followingId: Int, ): LoadResult<Unit>

    suspend fun getUserRegions(userId: Int): LoadResult<Set<String>>

    suspend fun addUserRegion(userId: Int, regionId: String): LoadResult<Unit>

    suspend fun removeUserRegion(userId: Int, regionId: String): LoadResult<Unit>
}