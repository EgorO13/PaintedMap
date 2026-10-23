package com.paintedmap.data

data class User(
    val userNickName: String,
    val userId: Int,
    val userName: String,
    private val regions: MutableSet<RegionEntry>,
) {
    init {
        require(userId > 0)
        require(userName.matches(Regex("^[A-Za-z0-9_]{3,20}$")))
        require(userName.isNotBlank() && userName.length < 30)
    }

    fun addRegion(region: RegionEntry) {
        regions.add(region)
    }

    fun amountOfRegions() : Int {
        return regions.size
    }

    fun getRegions() : MutableSet<RegionEntry> {
        return regions
    }
}
