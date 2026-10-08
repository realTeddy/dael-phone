package me.tewodros.dael.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.data.Contact

/** Full-screen "Mom is calling" overlay. Times out on its own if ignored. */
@Composable
fun IncomingCallScreen(contact: Contact, onAnswer: () -> Unit, onDecline: () -> Unit) {
    val speaker = LocalSpeaker.current
    LaunchedEffect(contact.id) {
        repeat(8) {
            Tones.ring()
            if (it % 3 == 0) speaker.say("${contact.name} is calling!")
            delay(2500)
        }
        onDecline()
    }
    val wiggle = rememberInfiniteTransition(label = "wiggle")
    val angle by wiggle.animateFloat(-12f, 12f, infiniteRepeatable(tween(120), RepeatMode.Reverse), label = "a")

    Box(Modifier.fillMaxSize().background(Palette.bg)) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Face(contact, 220.dp)
            Spacer(Modifier.height(24.dp))
            Text(contact.name, color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.ExtraBold)
            Text("is calling", color = Color.White.copy(alpha = 0.8f), fontSize = 26.sp)
        }
        Row(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            RoundButton(Palette.red, size = 96.dp, onClick = { Tones.blip(); onDecline() }) {
                Icon(Icons.Filled.CallEnd, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(48.dp))
            }
            RoundButton(Palette.mint, size = 110.dp, onClick = { Tones.blip(); onAnswer() }) {
                Icon(Icons.Filled.Call, contentDescription = "Answer", tint = Color.White, modifier = Modifier.size(56.dp).rotate(angle))
            }
        }
    }
}
