package me.tewodros.dael.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.Palette

internal data class NamedColor(val name: String, val color: Color)

internal val namedColors = listOf(
    NamedColor("Red", Color(0xFFEF4444)), NamedColor("Blue", Color(0xFF3B82F6)), NamedColor("Green", Color(0xFF22C55E)),
    NamedColor("Yellow", Color(0xFFFACC15)), NamedColor("Purple", Color(0xFFA855F7)), NamedColor("Pink", Color(0xFFF472B6)),
    NamedColor("Orange", Color(0xFFF97316)),
)

/**
 * "Find the red one!": a big swatch is shown and named; the child picks the same color from three
 * objects below. The options are different shapes so the match is by color, not outline.
 */
@Composable
fun ColorsGameScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    var roundNo by remember { mutableStateOf(0) }
    var solved by remember { mutableStateOf(false) }
    var wrongPick by remember { mutableStateOf(-1) }

    val round = remember(roundNo) {
        val picks = namedColors.shuffled().take(3)
        Triple(picks.random(), picks, Shape.entries.shuffled().take(3))
    }
    val (target, options, shapes) = round

    LaunchedEffect(roundNo) {
        solved = false; wrongPick = -1
        delay(400)
        speaker.say("Find the ${target.name.lowercase()} one!")
    }
    LaunchedEffect(solved) { if (solved) { delay(3000); roundNo++ } }

    Column(
        Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding().navigationBarsPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text("Colors", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 16.dp))
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            val pulse by animateFloatAsState(if (solved) 1.15f else 1f, spring(dampingRatio = 0.35f), label = "p")
            Box(
                Modifier
                    .size(230.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(target.color)
                    .clickable { Tones.blip(); speaker.say("Find the ${target.name.lowercase()} one!") },
            )
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            options.forEachIndexed { i, c ->
                val correct = c == target
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
                        .background(Palette.surface)
                        .clickable(enabled = !solved) {
                            if (correct) {
                                solved = true
                                Tones.play(880.0, 160)
                                speaker.say("${c.name}!") { speaker.say("Yay! You did it!") }
                            } else {
                                wrongPick = i
                                Tones.play(300.0, 160)
                                speaker.say("Try again!")
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(Modifier.fillMaxSize(0.7f)) {
                        drawShape(shapes[i], Offset(size.width / 2f, size.height / 2f), size.minDimension, c.color, Fill)
                    }
                }
            }
        }
    }
}
