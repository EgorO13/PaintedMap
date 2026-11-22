package com.paintedmap.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    @SerialName("id") val id: Int,
    @SerialName("nickname") val nickname: String,
    @SerialName("name") val name: String,
    @SerialName("subs_count") val subsCount: Int? = null,
    @SerialName("authors_count") val authorsCount: Int? = null,
    @SerialName("regions_count") val regionsCount: Int? = null,
)