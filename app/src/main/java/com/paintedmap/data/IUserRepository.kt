package com.paintedmap.data

interface IUserRepository {
    suspend fun findById(userId: Int): User?
    suspend fun findByNickname(userNickName: String): User?
    suspend fun searchAllUsers(query: String): List<User>
}