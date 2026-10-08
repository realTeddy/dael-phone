package me.tewodros.dael.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import me.tewodros.dael.data.Contact
import me.tewodros.dael.data.Repo
import me.tewodros.dael.data.Settings
import me.tewodros.dael.ui.games.AnimalsScreen
import me.tewodros.dael.ui.games.BubblesScreen
import me.tewodros.dael.ui.games.PaintScreen
import me.tewodros.dael.ui.games.PeekabooScreen
import me.tewodros.dael.ui.games.PianoScreen
import me.tewodros.dael.ui.parent.ParentGateScreen
import me.tewodros.dael.ui.parent.ParentSettingsScreen
import kotlin.random.Random

@Composable
fun DaelApp(repo: Repo, onExitApp: () -> Unit) {
    val nav = rememberNavController()
    val contacts by repo.contacts.collectAsStateWithLifecycle(initialValue = Contact.defaults)
    val settings by repo.settings.collectAsStateWithLifecycle(initialValue = Settings())
    val backStack by nav.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    var incoming by remember { mutableStateOf<Contact?>(null) }

    // Surprise calls: wait a random while, then ring if the child is on a "safe" screen.
    LaunchedEffect(settings.incomingCalls, settings.incomingEveryMinutes, contacts.size) {
        if (!settings.incomingCalls || contacts.isEmpty()) return@LaunchedEffect
        while (true) {
            val base = settings.incomingEveryMinutes * 60_000L
            delay(base + Random.nextLong(base / 2))
            // Read the live route here; the value captured when the effect started would be stale.
            val now = nav.currentBackStackEntry?.destination?.route
            val onSafeScreen = now != Routes.GATE && now != Routes.SETTINGS &&
                now?.startsWith("call/") != true && now != Routes.RANDOM_CALL
            if (incoming == null && onSafeScreen) incoming = contacts.random()
        }
    }

    // Back button never leaves the app. On the home screen it is simply swallowed.
    BackHandler(enabled = route == Routes.HOME) { }

    // Keep every screen clear of the camera cutout, rounded corners and gesture areas.
    Box(
        Modifier
            .fillMaxSize()
            .background(Palette.bg)
            .safeDrawingPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        AppNavHost(nav, repo, contacts, settings, onExitApp)
        incoming?.let { inc ->
            IncomingCallScreen(
                contact = inc,
                onAnswer = {
                    incoming = null
                    nav.navigate(Routes.call(inc.id) + "?answered=true")
                },
                onDecline = { incoming = null },
            )
        }
    }
}

@Composable
private fun AppNavHost(
    nav: NavHostController,
    repo: Repo,
    contacts: List<Contact>,
    settings: Settings,
    onExitApp: () -> Unit,
) {
    NavHost(nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onOpen = { nav.navigate(it) }, onParentGate = { nav.navigate(Routes.GATE) })
        }
        composable(Routes.DIALER) {
            DialerScreen(onBack = { nav.popBackStack() }, onCall = { nav.navigate(Routes.RANDOM_CALL) })
        }
        composable(Routes.RANDOM_CALL) {
            RandomCallScreen(settings.childName) { nav.popBackStack() }
        }
        composable(Routes.PHONE) {
            PhoneScreen(
                contacts,
                onBack = { nav.popBackStack() },
                onCall = { nav.navigate(Routes.call(it.id)) },
                onKeypad = { nav.navigate(Routes.DIALER) },
            )
        }
        composable(Routes.BUBBLES) { BubblesScreen { nav.popBackStack() } }
        composable("${Routes.CALL}?answered={answered}") { entry ->
            val id = entry.arguments?.getString("id")
            val answered = entry.arguments?.getString("answered") == "true"
            val contact = contacts.firstOrNull { it.id == id }
            if (contact == null) {
                LaunchedEffect(Unit) { nav.popBackStack() }
            } else {
                VideoCallScreen(contact, settings.childName, skipRinging = answered) { nav.popBackStack() }
            }
        }
        composable(Routes.ANIMALS) { AnimalsScreen { nav.popBackStack() } }
        composable(Routes.PIANO) { PianoScreen { nav.popBackStack() } }
        composable(Routes.PAINT) { PaintScreen { nav.popBackStack() } }
        composable(Routes.PEEKABOO) { PeekabooScreen(contacts) { nav.popBackStack() } }
        composable(Routes.GATE) {
            ParentGateScreen(
                onPass = { nav.navigate(Routes.SETTINGS) { popUpTo(Routes.HOME) } },
                onCancel = { nav.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            ParentSettingsScreen(repo, contacts, settings, onDone = { nav.popBackStack() }, onExitApp = onExitApp)
        }
    }
}
