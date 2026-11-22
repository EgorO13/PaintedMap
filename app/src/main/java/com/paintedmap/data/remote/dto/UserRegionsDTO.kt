package com.paintedmap.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRegionsDTO(
    @SerialName("items") val items: List<String>
)
