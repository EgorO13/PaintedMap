package com.paintedmap.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.paintedmap.data.ScreensEnum
import com.paintedmap.ui.theme.Dimens

@Composable
fun NavigationContent(
    onProfileClick: () -> Unit,
    onMapClick: () -> Unit,
    onSettingsClick: () -> Unit,
    currentScreen: ScreensEnum,
    modifier: Modifier = Modifier
) {
    val systemNavigationPadding = WindowInsets.navigationBars.asPaddingValues()

    GlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.navigationBarHeight + systemNavigationPadding.calculateBottomPadding())
            .padding(horizontal = Dimens.xs)
            .padding(top = Dimens.xs, bottom = systemNavigationPadding.calculateBottomPadding() + Dimens.xs),
        //shape = MaterialTheme.shapes.extraLarge,
        alpha = 1f,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.xs)
        ) {
            val profileColor = if (currentScreen == ScreensEnum.Profile) MaterialTheme.colorScheme.background.copy(alpha = 0.5f) else Color.Transparent
            val mapColor = if (currentScreen == ScreensEnum.Map) MaterialTheme.colorScheme.background.copy(alpha = 0.5f) else Color.Transparent
            val settingsColor = if (currentScreen == ScreensEnum.Settings) MaterialTheme.colorScheme.background.copy(alpha = 0.5f) else Color.Transparent

            ActionButton(
                onClick = onProfileClick,
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f).fillMaxHeight().padding(Dimens.sm),
                contentDescription = "Profile",
                containerColor = profileColor,
                iconColor = MaterialTheme.colorScheme.onSurface
            )

            ActionButton(
                onClick = onMapClick,
                icon = Icons.Default.Public,
                modifier = Modifier.weight(1f).fillMaxHeight().padding(Dimens.sm),
                contentDescription = "Map",
                containerColor = mapColor,
                iconColor = MaterialTheme.colorScheme.onSurface
            )

            ActionButton(
                onClick = onSettingsClick,
                icon = Icons.Default.Settings,
                modifier = Modifier.weight(1f).fillMaxHeight().padding(Dimens.sm),
                contentDescription = "Settings",
                containerColor = settingsColor,
                iconColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
