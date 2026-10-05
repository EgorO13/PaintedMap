package com.paintedmap.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MapScreen(onProfileClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.5f)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text("ЗАГЛУШКА КАРТЫ")
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.1f)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                val visited = 13
                val ALL = 89
                Text(
                    "СТАТИСТИКА: ${visited * 100 / ALL}% ($visited/$ALL)"                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .background(MaterialTheme.colorScheme.tertiary),
                contentAlignment = Alignment.Center
            ) {
                Text("ЗАГЛУШКА СПИСКА РЕГИОНОВ")
            }
        }

        FloatingActionButton(
            onClick = onProfileClick,
            shape = FloatingActionButtonDefaults.largeShape,
            modifier = Modifier.align(Alignment.TopStart).padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            Text("Profile")
        }
    }
}

//@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
//@Composable
//fun MapScreenPreview() {
//    com.example.paintedmap.ui.theme.PaintedMapTheme {
//        MapScreen(
//            onProfileClick = {}
//        )
//    }
//}