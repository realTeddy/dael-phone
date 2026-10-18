package me.tewodros.dael.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import me.tewodros.dael.audio.AudioRoute
import me.tewodros.dael.data.Contact
import me.tewodros.dael.data.Repo
import me.tewodros.dael.data.Settings
import me.tewodros.dael.ui.games.AnimalsScreen
import me.tewodros.dael.ui.games.BubblesScreen
import me.tewodros.dael.ui.games.PaintScreen
import me.tewodros.dael.ui.games.PeekabooScreen
import me.tewodros.dael.ui.games.PianoScreen
import me.tewodros.dael.ui.games.ShapesScreen
import me.tewodros.dael.ui.games.PuzzlesScreen
import me.tewodros.dael.ui.games.MatchScreen
import me.tewodros.dael.ui.games.JigsawScreen
import me.tewodros.dael.ui.games.ColorsGameScreen
import me.tewodros.dael.ui.games.CountGameScreen
import me.tewodros.dael.ui.games.SoundGameScreen
import me.tewodros.dael.ui.media.PhotosScreen
import me.tewodros.dael.ui.parent.ParentGateScreen
import me.tewodros.dael.ui.parent.ParentSettingsScreen
import kotlin.random.Random

@Composable
fun DaelApp(repo: Repo, onExitApp: () -> Unit) {
    val nav = remember { Nav(Routes.HOME) }
    val contacts by repo.contacts.collectAsStateWithLifecycle(initialValue = Contact.defaults)
    val settings by repo.settings.collectAsStateWithLifecycle(initialValue = Settings())
    val route = nav.current

    var incoming by remember { mutableStateOf<Contact?>(null) }

    LaunchedEffect(settings.speakerOnly) { AudioRoute.forceSpeaker = settings.speakerOnly }

    // Surprise calls: wait a random while, then ring if the child is on a "safe" screen.
    LaunchedEffect(settings.incomingCalls, settings.incomingEveryMinutes, contacts.size) {
        if (!settings.incomingCalls || contacts.isEmpty()) return@LaunchedEffect
        while (true) {
            val base = settings.incomingEveryMinutes * 60_000L
            delay(base + Random.nextLong(base / 2))
            val now = nav.current
            val onSafeScreen = now != Routes.GATE && now != Routes.SETTINGS &&
                !now.startsWith("call/") && now != Routes.RANDOM_CALL && now != Routes.PHOTOS
            if (incoming == null && onSafeScreen) incoming = contacts.random()
        }
    }

    // Every back press is ours. The stack never empties, so mashing Back can only reach Home.
    BackHandler(enabled = true) { nav.back() }

    Box(
        Modifier
            .fillMaxSize()
            .background(Palette.bg)
            .safeDrawingPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Screen(route, nav, repo, contacts, settings, onExitApp)
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
private fun Screen(
    route: String,
    nav: Nav,
    repo: Repo,
    contacts: List<Contact>,
    settings: Settings,
    onExitApp: () -> Unit,
) {
    when {
        route == Routes.HOME -> HomeScreen(onOpen = { nav.navigate(it) }, onParentGate = { nav.navigate(Routes.GATE) })
        route == Routes.PHONE -> PhoneScreen(
            contacts,
            onBack = { nav.back() },
            onCall = { nav.navigate(Routes.call(it.id)) },
            onKeypad = { nav.navigate(Routes.DIALER) },
        )
        route == Routes.DIALER -> DialerScreen(onBack = { nav.back() }, onCall = { nav.navigate(Routes.RANDOM_CALL) })
        route == Routes.RANDOM_CALL -> RandomCallScreen(settings.childName) { nav.back() }
        route.startsWith("call/") -> {
            val id = route.removePrefix("call/").substringBefore("?")
            val answered = route.substringAfter("?answered=", "") == "true"
            val contact = contacts.firstOrNull { it.id == id }
            if (contact == null) {
                LaunchedEffect(route) { nav.back() }
            } else {
                VideoCallScreen(contact, settings.childName, skipRinging = answered) { nav.back() }
            }
        }
        route == Routes.ANIMALS -> AnimalsScreen { nav.back() }
        route == Routes.PIANO -> PianoScreen { nav.back() }
        route == Routes.PAINT -> PaintScreen { nav.back() }
        route == Routes.PEEKABOO -> PeekabooScreen(contacts) { nav.back() }
        route == Routes.BUBBLES -> BubblesScreen { nav.back() }
        route == Routes.PHOTOS -> PhotosScreen { nav.back() }
        route == Routes.SHAPES -> ShapesScreen { nav.back() }
        route == Routes.PUZZLES -> PuzzlesScreen(onBack = { nav.back() }, onOpen = { nav.navigate(it) })
        route == Routes.MATCH -> MatchScreen { nav.back() }
        route == Routes.JIGSAW -> JigsawScreen { nav.back() }
        route == Routes.COLORS -> ColorsGameScreen { nav.back() }
        route == Routes.COUNT -> CountGameScreen { nav.back() }
        route == Routes.SOUND -> SoundGameScreen { nav.back() }
        route == Routes.GATE -> ParentGateScreen(
            onPass = { nav.navigate(Routes.SETTINGS, popUpTo = Routes.HOME) },
            onCancel = { nav.back() },
        )
        route == Routes.SETTINGS -> ParentSettingsScreen(repo, contacts, settings, onDone = { nav.back() }, onExitApp = onExitApp)
        else -> LaunchedEffect(route) { nav.home() }
    }
}
