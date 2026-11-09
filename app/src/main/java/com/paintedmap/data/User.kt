package com.paintedmap.data

data class User(
    val userNickName: String,
    val userId: Int,
    val userName: String,
    val userSubs: MutableSet<Int>,
    val userAuthors: MutableSet<Int>,

    private val userRegions: MutableSet<RegionEntry>,
) {
    init {
        require(userId > 0)
        require(userName.matches(Regex("^[A-Za-z0-9_]{3,20}$")))
        require(userName.isNotBlank() && userName.length < 30)
    }

    fun addRegion(region: RegionEntry) {
        userRegions.add(region)
    }

    fun amountOfRegions() : Int {
        return userRegions.size
    }

    fun getRegions() : Set<RegionEntry> {
        return userRegions
    }


    fun addAuthor(id: Int) {
        userAuthors.add(id)
    }

    fun amountOfAuthors() : Int {
        return userAuthors.size
    }

    fun getAuthors() : Set<Int> {
        return userAuthors
    }


    fun addSub(id: Int) {
        userSubs.add(id)
    }

    fun amountOfSubs() : Int {
        return userSubs.size
    }

    fun getSubs() : Set<Int> {
        return userSubs
    }
}
