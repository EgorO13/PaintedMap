package com.paintedmap.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paintedmap.ui.components.ActionButton
import com.paintedmap.ui.components.GlassPanel

@Composable
fun EmptyScreen(onEmptyClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        //Text("Empty", color = MaterialTheme.colorScheme.onSurface)
        GlassPanel(color = MaterialTheme.colorScheme.surface) {
            ActionButton(
                onClick = onEmptyClick,
                modifier = Modifier.size(80.dp),
                icon = Icons.Default.Refresh,
                contentDescription = "Reload",
                containerColor = Color.Transparent,
                iconColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
