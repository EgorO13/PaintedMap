package com.paintedmap.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPageDTO(
    @SerialName("items") val items: List<UserDTO>,
    @SerialName("next_cursor") val nextCursor: String? = null,
)
