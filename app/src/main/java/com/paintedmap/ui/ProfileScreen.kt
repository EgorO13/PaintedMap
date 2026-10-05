package com.paintedmap.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.paintedmap.data.User
import android.content.Intent
import androidx.compose.ui.platform.LocalContext

@Composable
fun ProfileScreen(user : User) {
    val localContext = LocalContext.current
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ")
            Text("@${user.userNickName}")
            Text(user.userName)
        }
        FloatingActionButton(
            onClick = {
                val link = "paintedmap://user/${user.userId}"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, link)
                }
                localContext.startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Share profile"
                    )
                )
            },
            shape = FloatingActionButtonDefaults.largeShape,
            modifier = Modifier.align(Alignment.TopEnd).padding(horizontal = 16.dp, vertical = 32.dp)
        ) {
            Text("Share")
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreview() {
//    PaintedMapTheme {
//        ProfileScreen(
//            user = User(
//                userId = 1,
//                userNickName = "ivan",
//                userName = "Иван Петров"
//            )
//        )
//    }
//}