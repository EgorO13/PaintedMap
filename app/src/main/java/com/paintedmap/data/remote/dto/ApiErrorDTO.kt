package com.paintedmap.data.remote.dto

import com.paintedmap.domain.model.NetworkError
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDTO(
    val code: String? = null
)

class ApiFailure(
    val statusCode: Int,
    val error: NetworkError,
) : Exception("HTTP $statusCode")