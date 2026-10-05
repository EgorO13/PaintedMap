package com.paintedmap.data

interface IUserRepository {
    fun findById(id: Int): User?
    fun findByNickname(nickname: String): User?
}