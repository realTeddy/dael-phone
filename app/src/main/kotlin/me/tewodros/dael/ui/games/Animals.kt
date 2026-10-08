package me.tewodros.dael.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.Palette

private data class Animal(val emoji: String, val name: String, val sound: String)

private val animals = listOf(
    Animal("🐶", "Dog", "Woof woof!"),
    Animal("🐱", "Cat", "Meow!"),
    Animal("🐮", "Cow", "Moo!"),
    Animal("🐷", "Pig", "Oink oink!"),
    Animal("🐔", "Chicken", "Cluck cluck!"),
    Animal("🦆", "Duck", "Quack quack!"),
    Animal("🐑", "Sheep", "Baa!"),
    Animal("🐴", "Horse", "Neigh!"),
    Animal("🦁", "Lion", "Roar!"),
    Animal("🐘", "Elephant", "Toot!"),
    Animal("🐸", "Frog", "Ribbit!"),
    Animal("🐵", "Monkey", "Ooh ooh ah ah!"),
)

@Composable
fun AnimalsScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    var active by remember { mutableStateOf(-1) }
    LaunchedEffect(active) { if (active >= 0) { delay(600); active = -1 } }

    Column(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text("Animals", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 16.dp))
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(animals) { i, a ->
                val scale by animateFloatAsState(if (active == i) 1.25f else 1f, spring(dampingRatio = 0.35f), label = "a")
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .scale(scale)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Palette.tiles[i % Palette.tiles.size])
                        .clickable {
                            active = i
                            Tones.play(300.0 + i * 50, 200)
                            speaker.say("${a.name}. ${a.sound}")
                        },
                    contentAlignment = Alignment.Center,
                ) { Text(a.emoji, fontSize = 60.sp) }
            }
        }
    }
}
