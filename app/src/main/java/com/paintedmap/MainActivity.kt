package com.paintedmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.deeplink.withBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.paintedmap.data.remote.RemoteUserDataSource
import com.paintedmap.data.remote.UserApiService
import com.paintedmap.data.remote.mock.LocalMockServer
import com.paintedmap.data.remote.mock.MockServerMode
import com.paintedmap.data.remote.network.NowTokenProvider
import com.paintedmap.data.remote.network.createHttpClient
import com.paintedmap.data.repository.NetworkUserRepository
import com.paintedmap.domain.repository.UserRepository
import com.paintedmap.ui.components.NavigationContent
import com.paintedmap.ui.screens.ErrorScreen
import com.paintedmap.ui.screens.SettingsScreen
import com.paintedmap.ui.screens.map.MapScreenContainer
import com.paintedmap.ui.screens.profile.ProfileScreenContainer
import com.paintedmap.ui.theme.PaintedMapTheme
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.serializer

class MainActivity : ComponentActivity() {

    private val profileDeepLinkMatcher = UriDeepLinkMatcher(
        uriPattern = "paintedmap://user/{userId}".toUri(),
        serializer = serializer<ScreenKey.Profile>()
    ).withBackStack { match ->
        listOf(ScreenKey.Map, match.key)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialStack: List<NavKey> = if (intent?.data != null) {
            val request = DeepLinkRequest(uri = intent.data)
            profileDeepLinkMatcher.match(request)?.backStack ?: listOf(ScreenKey.Map)
        } else {
            listOf(ScreenKey.Map)
        }

        setContent {
            PaintedMapTheme {
                val backStack = rememberNavBackStack(*initialStack.toTypedArray())

                LaunchedEffect(intent) {
                    if (intent?.data != null) {
                        val newRequest = DeepLinkRequest(uri = intent.data)
                        profileDeepLinkMatcher.match(newRequest)?.backStack?.let { newStack ->
                            backStack.clear()
                            backStack.addAll(newStack)
                        }
                    }
                }
                PaintedMapApp(backStack = backStack)
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
        setIntent(intent)
    }
}

class InsetsPaddingDecorator : NavEntryDecorator<NavKey>(
    decorate = { entry ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            entry.Content()
        }
    }
)

@Composable
private fun PaintedMapApp(
    backStack: NavBackStack<NavKey>
) {
    val mockServer = remember { LocalMockServer()/*.apply { mode = MockServerMode.CYCLE_ERRORS }*/ }

    // следим за жизнью экрана, что бы при смерти в корутине остановить сервер
    DisposableEffect(mockServer) {
        onDispose {
            CoroutineScope(Dispatchers.IO).launch {
                mockServer.stop()
            }
        }
    }

    val serverUrl by produceState<Url?>(initialValue = null, mockServer) {
        value = mockServer.startAndGetUrl()
    }

    val baseUrl = serverUrl
    if (baseUrl == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // запомнили репозиторий. обновим только при смене url
    val userRepository: UserRepository = remember(baseUrl) {
        val tokenProvider = NowTokenProvider("token")
        val httpClient = createHttpClient(
            baseUrl = baseUrl,
            tokens = tokenProvider
        )
        val apiService = UserApiService(httpClient)
        val remoteDataSource = RemoteUserDataSource(apiService)
        NetworkUserRepository(remoteDataSource)
    }

    val currentKey = backStack.lastOrNull() ?: ScreenKey.Map
    val insetsDecorator = remember { InsetsPaddingDecorator() }
    val savedProfileIds = rememberSaveable { mutableStateListOf<Int>(1) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationContent(
                currentKey = currentKey as ScreenKey,
                onMapClick = {
                    if (currentKey != ScreenKey.Map) {
                        backStack.add(ScreenKey.Map)
                    }
                },
                onProfileClick = {
                    if (currentKey !is ScreenKey.Profile) {
                        savedProfileIds.forEach { id ->
                            backStack.add(ScreenKey.Profile(userId = id))
                        }
                    } else if (currentKey != ScreenKey.Profile(userId = 1)) {
                        savedProfileIds.clear()
                        savedProfileIds.add(1)
                        backStack.add(ScreenKey.Profile(userId = 1))
                    }
                },
                onSettingsClick = {
                    if (currentKey != ScreenKey.Settings) {
                        backStack.add(ScreenKey.Settings)
                    }
                }
            )
        }
    ) { innerPadding ->
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.lastOrNull() is ScreenKey.Profile) {
                    if (backStack.size > 1 && backStack[backStack.size - 2] is ScreenKey.Profile) {
                        backStack.removeLastOrNull()
                        savedProfileIds.removeLastOrNull()
                    } else {
                        backStack.clear()
                        backStack.add(ScreenKey.Map)
                    }
                } else if (backStack.lastOrNull() is ScreenKey.Map) {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                } else if (backStack.lastOrNull() is ScreenKey.Settings) {
                    backStack.clear()
                    backStack.add(ScreenKey.Map)
                } else {
                    backStack.removeLastOrNull()
                }
            },
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
                insetsDecorator
            ),
            entryProvider = { key ->
                when (key) {
                    is ScreenKey.Map -> NavEntry(key) {
                        MapScreenContainer(
                            innerPadding = innerPadding,
                            userRepository = userRepository
                        )
                    }

                    is ScreenKey.Profile -> NavEntry(key) {
                        ProfileScreenContainer(
                            userId = key.userId,
                            innerPadding = innerPadding,
                            userRepository = userRepository,
                            onAuthorNavigate = { selectedAuthorId ->
                                backStack.add(ScreenKey.Profile(userId = selectedAuthorId))
                                savedProfileIds.add(selectedAuthorId)
                            }
                        )
                    }

                    is ScreenKey.Settings -> NavEntry(key) {
                        SettingsScreen()
                    }

                    is ScreenKey.Error -> NavEntry(key) {
                        ErrorScreen(
                            onErrorClick = {
                                backStack.clear()
                                backStack.add(ScreenKey.Map)
                            }
                        )
                    }

                    else -> NavEntry(key) {}
                }
            }
        )
    }
}