package me.tewodros.dael.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.data.Contact
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.Face
import me.tewodros.dael.ui.Palette

/** Tap the hands to reveal a family member. Object permanence, the toddler classic. */
@Composable
fun PeekabooScreen(contacts: List<Contact>, onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    var revealed by remember { mutableStateOf(false) }
    var who by remember { mutableStateOf(contacts.randomOrNull()) }
    val scale by animateFloatAsState(if (revealed) 1.2f else 1f, spring(dampingRatio = 0.3f), label = "p")

    Box(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding()) {
        Column(
            Modifier
                .fillMaxSize()
                .clickable {
                    revealed = !revealed
                    if (revealed) {
                        Tones.play(660.0, 200)
                        speaker.say("Peekaboo! It's ${who?.name ?: "you"}!")
                    } else {
                        Tones.play(330.0, 200)
                        who = contacts.randomOrNull()
                        speaker.say("Where did ${who?.name ?: "everyone"} go?")
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            Box(Modifier.scale(scale), contentAlignment = Alignment.Center) {
                if (revealed && who != null) Face(who!!, 240.dp) else Text("🙈", fontSize = 180.sp)
            }
            Spacer(Modifier.height(24.dp))
            Text(
                if (revealed) "Peekaboo!" else "Where is ${who?.name ?: "everyone"}?",
                color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
            )
        }
        // Drawn last so it sits above the full-screen tap target.
        BackButton(onBack, Modifier.padding(16.dp))
    }
}
