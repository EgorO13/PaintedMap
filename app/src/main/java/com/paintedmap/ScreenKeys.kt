package com.paintedmap

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface ScreenKey : NavKey {
    @Serializable data object Map : ScreenKey
    @Serializable data class Profile(val userId: Int) : ScreenKey
    @Serializable data object Settings : ScreenKey
    @Serializable data object Error : ScreenKey
}