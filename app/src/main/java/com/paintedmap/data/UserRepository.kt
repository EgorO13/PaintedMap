package com.paintedmap.data

class UserRepository : IUserRepository{
    private val users = listOf (
        User ("admin", 1, "Egor_Osoka", mutableSetOf<RegionEntry>()),
        User ("dlmevel", 2, "Eva_D", mutableSetOf<RegionEntry>())
   )

    override fun findByNickname(userNickName : String) : User? {
        return users.find { it.userNickName == userNickName }
    }

    override fun findById(userId : Int) : User? {
        return users.find { it.userId == userId }
    }
}