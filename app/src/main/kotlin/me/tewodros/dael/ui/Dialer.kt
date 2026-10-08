package me.tewodros.dael.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones

private val keys = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf("⭐", "0", "❤️"),
)

private val digitWords = mapOf(
    "0" to "zero", "1" to "one", "2" to "two", "3" to "three", "4" to "four",
    "5" to "five", "6" to "six", "7" to "seven", "8" to "eight", "9" to "nine",
    "⭐" to "star", "❤️" to "heart",
)

/** Dial pad that says every key out loud. Dialing never reaches a real network. */
@Composable
fun DialerScreen(onBack: () -> Unit, onCall: () -> Unit) {
    var number by rememberSaveable { mutableStateOf("") }
    val speaker = LocalSpeaker.current

    Column(
        Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text(
                number.ifEmpty { "☎️" },
                color = Color.White,
                fontSize = if (number.length > 8) 30.sp else 42.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            RoundButton(Palette.surface, size = 64.dp, onClick = { Tones.blip(); number = number.dropLast(1) }) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(32.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        keys.forEachIndexed { row, line ->
            Row(
                Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                line.forEachIndexed { col, key ->
                    val color = Palette.tiles[(row * 3 + col) % Palette.tiles.size]
                    RoundButton(color, size = 96.dp, onClick = {
                        Tones.play(440.0 + (row * 3 + col) * 40, 180)
                        speaker.say(digitWords[key] ?: key)
                        if (number.length < 12) number += key
                    }) {
                        Text(key, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            RoundButton(Palette.mint, size = 96.dp, onClick = { Tones.ring(); onCall() }) {
                Icon(Icons.Filled.Call, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }
    }
}
