package me.tewodros.dael.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.tewodros.dael.R
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.Palette

/**
 * "Listen! Who is it?": a real animal sound plays; the child picks which of three animals made it.
 * The big speaker button replays the sound. Correct: name, sound again, cheer.
 */
@Composable
fun SoundGameScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    var roundNo by remember { mutableStateOf(0) }
    var solved by remember { mutableStateOf(false) }
    var wrongPick by remember { mutableStateOf(-1) }
    var playing by remember { mutableStateOf(false) }

    val round = remember(roundNo) {
        val picks = farmAnimals.shuffled().take(3)
        picks.random() to picks
    }
    val (target, options) = round

    fun playPrompt() {
        playing = true
        speaker.say("Listen! Who is it?") { speaker.sound(target.key, interrupt = false) { playing = false } }
    }

    LaunchedEffect(roundNo) {
        solved = false; wrongPick = -1
        delay(400)
        playPrompt()
    }
    LaunchedEffect(solved) { if (solved) { delay(3600); roundNo++ } }

    Column(
        Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding().navigationBarsPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text("Who is it?", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 16.dp))
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val pulse by animateFloatAsState(if (playing) 1.12f else 1f, spring(dampingRatio = 0.35f), label = "p")
            Box(
                Modifier
                    .size(230.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(if (solved) Palette.mint else Palette.sky)
                    .clickable(enabled = !solved) { Tones.blip(); playPrompt() },
                contentAlignment = Alignment.Center,
            ) {
                if (solved) Icon(painterResource(target.icon), target.name, tint = Color.Unspecified, modifier = Modifier.fillMaxSize(0.75f))
                else Icon(painterResource(R.drawable.ic_tile_sound), "Play sound", tint = Color.Unspecified, modifier = Modifier.fillMaxSize(0.6f))
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            options.forEachIndexed { i, a ->
                val correct = a == target
                val scale by animateFloatAsState(
                    if (solved && correct) 1.12f else if (wrongPick == i) 0.85f else 1f,
                    spring(dampingRatio = 0.4f), label = "o",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .scale(scale)
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (solved && correct) Palette.mint else Palette.tiles[i % Palette.tiles.size])
                        .clickable(enabled = !solved) {
                            if (correct) {
                                solved = true
                                Tones.play(880.0, 160)
                                speaker.sayThenSound("${a.name}!", a.key) { speaker.say("Yay! You did it!") }
                            } else {
                                wrongPick = i
                                Tones.play(300.0, 160)
                                speaker.say("Try again!")
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(a.icon), a.name, tint = Color.Unspecified, modifier = Modifier.fillMaxSize(0.75f))
                }
            }
        }
    }
}
