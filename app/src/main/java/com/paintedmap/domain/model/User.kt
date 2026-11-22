package com.paintedmap.domain.model

data class User(
    val userId: Int,
    val userNickName: String,
    val userName: String,
    val userSubsCount: Int = 0,
    val userAuthorsCount: Int = 0,
    val userRegionsCount: Int = 0
)