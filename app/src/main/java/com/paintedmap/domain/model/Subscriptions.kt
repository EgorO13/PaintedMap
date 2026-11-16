package com.paintedmap.domain.model

data class Subscriptions(
    val subscriptions: MutableMap<Int, MutableSet<Int>> = mutableMapOf()
) {
    fun add(followerId: Int, followingId: Int) {
        subscriptions.getOrPut(followerId) { mutableSetOf() }.add(followingId)
    }

    fun del(followerId: Int, followingId: Int) {
        subscriptions[followerId]?.remove(followingId)
    }

    fun getFollowing(followerId: Int): Set<Int> {
        return subscriptions[followerId]?.toSet() ?: emptySet()
    }
}