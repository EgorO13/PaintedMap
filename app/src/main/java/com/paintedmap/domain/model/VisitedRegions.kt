package com.paintedmap.domain.model

data class VisitedRegions(
    val visited: MutableMap<Int, MutableSet<String>> = mutableMapOf()
) {
    fun add(userId: Int, regionId: String) {
        visited.getOrPut(userId) { mutableSetOf() }.add(regionId)
    }

    fun remove(userId: Int, regionId: String) {
        visited[userId]?.remove(regionId)
    }

    fun getRegionsForUser(userId: Int): Set<String> {
        return visited[userId]?.toSet() ?: emptySet()
    }
}
