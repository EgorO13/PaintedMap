package com.paintedmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.paintedmap.data.IUserRepository
import com.paintedmap.data.UserRepository
import com.paintedmap.ui.MapScreen
import com.paintedmap.ui.ProfileScreen
import com.paintedmap.ui.theme.PaintedMapTheme

class MainActivity : ComponentActivity() {

    // mutableStateOf заставляет Compose автоматически перерисовывать экран при изменении этих переменных
    private var currentScreen by mutableStateOf("main")
    private var selectedUserId by mutableStateOf<Int?>(null)
    private var errMessage by mutableStateOf<String?>(null)

    private val uRepo: IUserRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("myTagLC", "onCreate")
        enableEdgeToEdge()

        if (savedInstanceState == null) handleIntent(intent)
        else {
            currentScreen = savedInstanceState.getString("KEY_SCREEN", "main")
            if (savedInstanceState.containsKey("KEY_USER_ID")) {
                selectedUserId = savedInstanceState.getInt("KEY_USER_ID")
            }
            errMessage = savedInstanceState.getString("KEY_ERROR")
            Log.d("myTagLC", "Восстановились данные из Bundle")
        }

        setContent {
            PaintedMapTheme {
                PaintedMapApp(
                    currentScreen = currentScreen,
                    selectedUserId = selectedUserId,
                    errorMessage = errMessage,
                    uRepo = uRepo,
                    onNavigateToProfile = { userId ->
                        selectedUserId = userId
                        currentScreen = "profile"
                    },
                    onNavigateToMain = {
                        currentScreen = "main"
                        selectedUserId = null
                    },
                    onErrorShown = {
                        errMessage = null
                    }
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("KEY_SCREEN", currentScreen)
        selectedUserId?.let { outState.putInt("KEY_USER_ID", it) }
        errMessage?.let { outState.putString("KEY_ERROR", it) }
        Log.d("myTagLC", "onSaveInstanceState")
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme != "paintedmap" || uri.host != "user") {
            errMessage = "Incorrect link"
            return
        }
        val userId = uri.pathSegments.singleOrNull()?.toIntOrNull()
        if (userId == null || userId <= 0) {
            errMessage = "Incorrect link"
            return
        }
        val user = uRepo.findById(userId)
        if (user != null) {
            selectedUserId = userId
            currentScreen = "profile"
        } else {
            errMessage = "Profile not found"
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("myTagLC", "onNewIntent")
        setIntent(intent)
        handleIntent(intent)
    }
    override fun onStart() { super.onStart(); Log.d("myTagLC", "onStart") }
    override fun onResume() { super.onResume(); Log.d("myTagLC", "onResume") }
    override fun onPause() { Log.d("myTagLC", "onPause"); super.onPause() }
    override fun onStop() { Log.d("myTagLC", "onStop"); super.onStop() }
    override fun onDestroy() { Log.d("myTagLC", "onDestroy"); super.onDestroy() }
    override fun onRestart() { super.onRestart(); Log.d("myTagLC", "onRestart") }
}

@Composable
fun PaintedMapApp(
    currentScreen: String,
    selectedUserId: Int?,
    errorMessage: String?,
    uRepo: IUserRepository,
    onNavigateToProfile: (Int) -> Unit,
    onNavigateToMain: () -> Unit,
    onErrorShown: () -> Unit
) {
    val context = LocalContext.current
    val currentUser = remember { uRepo.findById(1) }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onErrorShown()
        }
    }

    Scaffold { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (currentScreen) {
                 "main" -> {
                    MapScreen(
                        onProfileClick = {
                            currentUser?.let { user ->
                                onNavigateToProfile(user.userId)
                            }
                        }
                    )
                }
                "profile" -> {
                    if (selectedUserId != null) {
                        val user = uRepo.findById(selectedUserId)
                        if (user != null) {
                            androidx.activity.compose.BackHandler {
                                onNavigateToMain()
                            }
                            ProfileScreen(user)
                        }
                        else onNavigateToMain()
                    }
                }
            }
        }
    }
}
