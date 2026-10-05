package com.paintedmap.data

data class User(
    val userNickName: String,
    val userId: Int,
    val userName: String
) {
    init {
        require(userId > 0)
        require(userName.matches(Regex("^[A-Za-z0-9_]{3,20}$")))
        require(userName.isNotBlank() && userName.length < 30)
    }
}
