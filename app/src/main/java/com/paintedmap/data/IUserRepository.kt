package com.paintedmap.data

interface IUserRepository {
    fun findById(userId: Int): User?
    fun findByNickname(userNickName: String): User?
}