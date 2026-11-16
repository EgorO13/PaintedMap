package com.paintedmap.data.remote.mapper

import com.paintedmap.domain.model.UserPage
import com.paintedmap.data.remote.dto.UserPageDTO

fun UserPageDTO.toDomain(): UserPage {
    return UserPage(
        items = items.map { it.toDomain() },
        nextCursor = nextCursor,
    )
}