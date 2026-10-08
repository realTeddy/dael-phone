package me.tewodros.dael.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import me.tewodros.dael.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.camera.SelfieView
import me.tewodros.dael.data.Contact
import java.io.File

private enum class Phase { RINGING, CONNECTED, ENDED }

/**
 * Fake video call. Rings briefly, then plays one of the contact's recorded clips.
 * With no clip it shows the face, says hello, and hangs up on its own.
 */
@Composable
fun VideoCallScreen(contact: Contact, childName: String, skipRinging: Boolean, onEnd: () -> Unit) {
    val context = LocalContext.current
    val speaker = LocalSpeaker.current
    var phase by remember { mutableStateOf(if (skipRinging) Phase.CONNECTED else Phase.RINGING) }
    val video = remember(contact) { contact.videoPaths.map(::File).filter { it.exists() }.randomOrNull() }

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) phase = Phase.ENDED
                }
            })
        }
    }
    DisposableEffect(Unit) { onDispose { player.release() } }

    LaunchedEffect(Unit) {
        if (phase == Phase.RINGING) {
            repeat(2) { Tones.ring(); delay(1500) }
            phase = Phase.CONNECTED
        }
    }

    LaunchedEffect(phase) {
        when (phase) {
            Phase.CONNECTED -> {
                if (video != null) {
                    player.setMediaItem(MediaItem.fromUri(video.toURI().toString()))
                    player.prepare()
                    player.playWhenReady = true
                } else {
                    speaker.say("Hi $childName! It's ${contact.name}! I love you! Bye bye!")
                    delay(12_000)
                    phase = Phase.ENDED
                }
            }
            Phase.ENDED -> {
                delay(600)
                onEnd()
            }
            else -> Unit
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (phase == Phase.CONNECTED && video != null) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
            )
        } else {
            val pulse = rememberInfiniteTransition(label = "pulse")
            val scale by pulse.animateFloat(1f, 1.12f, infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "s")
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                Face(contact, 220.dp, Modifier.scale(if (phase == Phase.RINGING) scale else 1f))
                Spacer(Modifier.height(24.dp))
                Text(contact.name, color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    when (phase) { Phase.RINGING -> "Calling…"; Phase.CONNECTED -> "Connected"; Phase.ENDED -> "Bye bye!" },
                    color = Color.White.copy(alpha = 0.8f), fontSize = 24.sp,
                )
            }
        }

        // Child's own face in the corner, like a real video call.
        if (phase == Phase.CONNECTED) {
            SelfieView(
                Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(110.dp, 150.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(32.dp)) {
            RoundButton(Palette.red, size = 96.dp, onClick = { Tones.blip(); player.stop(); speaker.stop(); onEnd() }) {
                Icon(Icons.Filled.CallEnd, contentDescription = "Hang up", tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }
    }
}

private data class Character(val icon: Int, val name: String, val key: String)

private val characters = listOf(
    Character(R.drawable.ic_lion, "Lion", "lion"),
    Character(R.drawable.ic_frog, "Frog", "frog"),
    Character(R.drawable.ic_cow, "Cow", "cow"),
    Character(R.drawable.ic_chicken, "Chicken", "chicken"),
    Character(R.drawable.ic_dog, "Puppy", "dog"),
    Character(R.drawable.ic_cat, "Kitty", "cat"),
    Character(R.drawable.ic_firetruck, "Fire truck", "firetruck"),
    Character(R.drawable.ic_train, "Train", "train"),
    Character(R.drawable.ic_robot, "Robot", "robot"),
)

/** What happens when the child dials any number: a silly character picks up. */
@Composable
fun RandomCallScreen(childName: String, onEnd: () -> Unit) {
    val speaker = LocalSpeaker.current
    val who = remember { characters.random() }
    var connected by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2500)
        connected = true
        var finished = false
        speaker.sayThenSound("Hello $childName! This is the ${who.name}!", who.key) {
            speaker.say("Bye bye!") { finished = true }
        }
        // Wait for the sequence, with a ceiling so a playback hiccup never strands the screen.
        var waited = 0
        while (!finished && waited < 15_000) { delay(200); waited += 200 }
        delay(800)
        onEnd()
    }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(1f, 1.15f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "s")

    Box(Modifier.fillMaxSize().background(Palette.bg)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
            Icon(painterResource(who.icon), who.name, tint = Color.Unspecified, modifier = Modifier.size(240.dp).scale(if (connected) scale else 1f))
            Spacer(Modifier.height(16.dp))
            Text(if (connected) who.name else "Ring ring…", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
        }
        Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(32.dp)) {
            RoundButton(Palette.red, size = 96.dp, onClick = { Tones.blip(); speaker.stop(); onEnd() }) {
                Icon(Icons.Filled.CallEnd, contentDescription = "Hang up", tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }
    }
}
