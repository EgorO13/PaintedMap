package com.paintedmap.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.paintedmap.ui.components.ActionButton
import com.paintedmap.ui.components.GlassPanel
@Composable
fun ErrorScreen(onErrorClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(104.dp)
        )
        Text("Error", color = MaterialTheme.colorScheme.error)
        GlassPanel(shape = RoundedCornerShape(28.dp)) {
            ActionButton(
                onClick = onErrorClick,
                modifier = Modifier.size(80.dp),
                icon = Icons.Default.Refresh,
                contentDescription = "Retry",
                containerColor = Color.Transparent,
                iconColor = MaterialTheme.colorScheme.onError
            )
        }
    }
}
