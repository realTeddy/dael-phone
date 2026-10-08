package me.tewodros.dael.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.Palette

private val notes = listOf(
    "C" to 261.63, "D" to 293.66, "E" to 329.63, "F" to 349.23,
    "G" to 392.00, "A" to 440.00, "B" to 493.88, "C" to 523.25,
)

@Composable
fun PianoScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text("Piano 🎹", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 16.dp))
        }
        Column(Modifier.fillMaxSize().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            notes.forEachIndexed { i, (name, hz) ->
                var lit by remember { mutableStateOf(false) }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (lit) Color.White else Palette.tiles[i % Palette.tiles.size])
                        .pointerInput(Unit) {
                            detectTapGestures(onPress = {
                                lit = true
                                Tones.play(hz, 600)
                                tryAwaitRelease()
                                lit = false
                            })
                        },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(name, color = if (lit) Palette.bg else Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 24.dp))
                }
            }
        }
    }
}
