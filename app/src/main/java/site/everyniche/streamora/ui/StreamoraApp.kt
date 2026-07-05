package site.everyniche.streamora.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import site.everyniche.streamora.AppContainer
import site.everyniche.streamora.data.model.ContentItem
import site.everyniche.streamora.ui.components.BottomNavBar
import site.everyniche.streamora.ui.components.NavTab
import site.everyniche.streamora.ui.screens.*
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column

private sealed class Screen {
    object Splash : Screen()
    object Onboarding : Screen()
    object Auth : Screen()
    object Main : Screen()
}

@Composable
fun StreamoraApp(container: AppContainer) {
    var screen by remember { mutableStateOf<Screen>(Screen.Splash) }
    val user by container.auth.currentUserFlow.collectAsState(initial = null)

    when (val s = screen) {
        Screen.Splash -> SplashScreen(onComplete = {
            screen = if (user != null) Screen.Main else Screen.Onboarding
        })
        Screen.Onboarding -> OnboardingScreenSimple(onComplete = { screen = Screen.Auth })
        Screen.Auth -> AuthScreen(auth = container.auth, onComplete = { screen = Screen.Main })
        Screen.Main -> MainAppScaffold(container)
    }
}

@Composable
private fun MainAppScaffold(container: AppContainer) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(NavTab.HOME) }
    var selectedItem by remember { mutableStateOf<ContentItem?>(null) }
    var browseState by remember { mutableStateOf<Pair<String, Map<String, String>>?>(null) }
    var loggedOut by remember { mutableStateOf(false) }

    if (loggedOut) {
        // Simplest way back to auth without a full nav graph rewrite: recreate.
        AuthScreen(auth = container.auth, onComplete = { loggedOut = false })
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            selectedItem != null -> MovieDetailScreen(
                container = container,
                baseItem = selectedItem!!,
                onBack = { selectedItem = null },
                onPlay = { item ->
                    (context as? ComponentActivity)?.startActivity(PlayerActivity.intentFor(context, item))
                },
            )
            browseState != null -> BrowseScreen(
                container = container,
                title = browseState!!.first,
                params = browseState!!.second,
                onBack = { browseState = null },
                onSelect = { selectedItem = it },
            )
            else -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f)) {
                        when (tab) {
                            NavTab.HOME -> HomeScreen(
                                container = container,
                                onSelect = { selectedItem = it },
                                onSearch = { tab = NavTab.SEARCH },
                                onBrowse = { title, params -> browseState = title to params },
                            )
                            NavTab.SEARCH -> SearchScreen(container = container, onSelect = { selectedItem = it })
                            NavTab.DOWNLOADS -> DownloadsScreen(container = container, onSelect = { selectedItem = it })
                            NavTab.PROFILE -> ProfileScreen(container = container, onLogout = { loggedOut = true })
                        }
                    }
                    BottomNavBar(active = tab, onChange = { tab = it })
                }
            }
        }
    }
}
