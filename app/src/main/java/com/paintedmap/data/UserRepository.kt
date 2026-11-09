package com.paintedmap.data

class UserRepository : IUserRepository{
    private val users = listOf (
        User ("test", 1, "Test_User", mutableSetOf<Int>(), mutableSetOf<Int>(2, 3, 4, 5, 6, 7, 8), mutableSetOf<RegionEntry>()),
        User ("EgorOs13", 2, "Egor_Osoka", mutableSetOf<Int>(), mutableSetOf<Int>(), mutableSetOf<RegionEntry>()),
        User ("batmen", 3, "Batmen", mutableSetOf<Int>(), mutableSetOf<Int>(), mutableSetOf<RegionEntry>()),
        User ("dlmevel", 4, "Eva_D", mutableSetOf<Int>(), mutableSetOf<Int>(2, 5), mutableSetOf<RegionEntry>()),
        User ("spider", 5, "Spider_Man", mutableSetOf<Int>(), mutableSetOf<Int>(), mutableSetOf<RegionEntry>()),
        User ("tonyplayboy", 6, "Iron_Man", mutableSetOf<Int>(), mutableSetOf<Int>(), mutableSetOf<RegionEntry>()),
        User ("green", 7, "Hulk", mutableSetOf<Int>(), mutableSetOf<Int>(), mutableSetOf<RegionEntry>()),
        User ("Odinson", 8, "Thor", mutableSetOf<Int>(), mutableSetOf<Int>(6), mutableSetOf<RegionEntry>()),
        )

    override fun findByNickname(userNickName : String) : User? {
        return users.find { it.userNickName == userNickName }
    }

    override fun findById(userId : Int) : User? {
        return users.find { it.userId == userId }
    }
}