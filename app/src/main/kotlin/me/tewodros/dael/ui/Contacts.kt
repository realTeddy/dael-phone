package me.tewodros.dael.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.data.Contact

/** The "phone app": favorite people to video-call, plus a keypad button for free dialing. */
@Composable
fun PhoneScreen(contacts: List<Contact>, onBack: () -> Unit, onCall: (Contact) -> Unit, onKeypad: () -> Unit) {
    val speaker = LocalSpeaker.current
    Column(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text("Phone", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 16.dp))
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(contacts, key = { it.id }) { c ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(Palette.surface)
                        .clickable {
                            Tones.ring()
                            speaker.say("Calling ${c.name}")
                            onCall(c)
                        }
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Face(c, 120.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(c.name, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        // Keypad: a wide bar so it is easy to hit and clearly different from the faces.
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Palette.mint)
                .clickable { Tones.blip(); onKeypad() }
                .padding(vertical = 18.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Dialpad, contentDescription = "Keypad", tint = Color.White, modifier = Modifier.size(40.dp))
            Spacer(Modifier.size(12.dp))
            Text("Keypad", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}
