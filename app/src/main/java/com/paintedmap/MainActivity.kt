package com.paintedmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.paintedmap.data.ScreensEnum
import com.paintedmap.data.UserRepository
import com.paintedmap.ui.components.NavigationContent
import com.paintedmap.ui.screens.ErrorScreen
import com.paintedmap.ui.screens.MapScreen
import com.paintedmap.ui.screens.ProfileScreen
import com.paintedmap.ui.screens.SettingsScreen
import com.paintedmap.ui.theme.Dimens
import com.paintedmap.ui.theme.PaintedMapTheme

private object Routes {
    const val Map = "map"
    const val Profile = "profile/{userId}"
    const val Settings = "settings"
    const val Error = "error"

    fun profile(userId: Int) = "profile/$userId"
}

class MainActivity : ComponentActivity() {

    private var pendingDeepLink by mutableStateOf<Intent?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")

        enableEdgeToEdge()

        if (savedInstanceState == null) {
            pendingDeepLink = intent.takeIf { it.data != null }
        }

        setContent {
            PaintedMapTheme {
                PaintedMapApp(
                    deepLinkIntent = pendingDeepLink,
                    onDeepLinkHandled = {
                        pendingDeepLink = null
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume")
    }

    override fun onPause() {
        Log.d("MainActivity", "onPause")
        super.onPause()
    }

    override fun onStop() {
        Log.d("MainActivity", "onStop")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy")
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent: ${intent.data}")
        setIntent(intent)
        pendingDeepLink = intent.takeIf { it.data != null }
    }
}

@Composable
private fun PaintedMapApp(
    deepLinkIntent: Intent?,
    onDeepLinkHandled: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userRepository = remember { UserRepository() }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    LaunchedEffect(deepLinkIntent) {
        val intent = deepLinkIntent ?: return@LaunchedEffect
        val uri = intent.data

        if (uri == null) {
            onDeepLinkHandled()
            return@LaunchedEffect
        }

        val handled = navController.handleDeepLink(intent)

        if (!handled) {
            Toast.makeText(
                context,
                "Incorrect link",
                Toast.LENGTH_SHORT
            ).show()
        }

        onDeepLinkHandled()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationContent(
                currentScreen = when (currentRoute) {
                    Routes.Profile -> ScreensEnum.Profile
                    Routes.Settings -> ScreensEnum.Settings
                    else -> ScreensEnum.Map
                },
                onMapClick = {
                    navController.navigate(Routes.Map) {
                        launchSingleTop = true
                    }
                },
                onProfileClick = {
                    navController.navigate(Routes.profile(1)) {
                        launchSingleTop = true
                    }
                },
                onSettingsClick = {
                    navController.navigate(Routes.Settings) {
                        launchSingleTop = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Map,
            modifier = Modifier.fillMaxSize().statusBarsPadding()
        ) {
            composable(Routes.Map) {
                MapScreen(innerPadding = innerPadding)
            }

            composable(
                route = Routes.Profile,
                arguments = listOf(
                    navArgument("userId") {
                        type = NavType.IntType
                    }
                ),
                deepLinks = listOf(
                    navDeepLink {
                        uriPattern = "paintedmap://user/{userId}"
                    }
                )
            ) { entry ->
                val userId = entry.arguments?.getInt("userId")
                val user = userId?.let { userRepository.findById(it) }

                if (user != null) {
                    ProfileScreen(user = user, innerPadding = innerPadding)
                } else {
                    LaunchedEffect(userId) {
                        Toast.makeText(
                            context,
                            "User not found",
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.navigate(Routes.Map) {
                            popUpTo(Routes.Map) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    }
                }
            }

            composable(Routes.Settings) {
                SettingsScreen()
            }

            composable(Routes.Error) {
                ErrorScreen(
                    onErrorClick = {
                        navController.navigate(Routes.Map) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

