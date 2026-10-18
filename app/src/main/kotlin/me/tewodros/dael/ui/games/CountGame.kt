package me.tewodros.dael.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.tewodros.dael.R
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.Palette
import kotlin.random.Random

private data class CountItem(val icon: Int, val singular: String, val plural: String)

private val countItems = listOf(
    CountItem(R.drawable.ic_apple, "apple", "apples"),
    CountItem(R.drawable.ic_star_gold, "star", "stars"),
    CountItem(R.drawable.ic_heart_pink, "heart", "hearts"),
)
private val numberNames = listOf("One", "Two", "Three", "Four", "Five")

/**
 * Tap-to-count. A few apples (or stars, or hearts) are scattered; tapping each one counts it aloud
 * and marks it with its number. When all are tapped: "Three apples!" and a cheer. This is one-to-one
 * correspondence, the step after reciting number words.
 */
@Composable
fun CountGameScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    var roundNo by remember { mutableStateOf(0) }
    var done by remember { mutableStateOf(false) }
    val tapped = remember(roundNo) { mutableStateListOf<Int>() }

    val round = remember(roundNo) {
        val n = (roundNo % 5) + 1            // 1, 2, 3, 4, 5, then around again
        val item = countItems[(roundNo / 5 + roundNo) % countItems.size]
        // Positions on a loose grid with jitter so they never overlap.
        val cells = listOf(0.25f to 0.3f, 0.75f to 0.3f, 0.5f to 0.52f, 0.25f to 0.74f, 0.75f to 0.74f).shuffled().take(n)
        val spots = cells.map { (x, y) -> Pair(x + Random.nextFloat() * 0.1f - 0.05f, y + Random.nextFloat() * 0.06f - 0.03f) }
        Triple(n, item, spots)
    }
    val (n, item, spots) = round

    LaunchedEffect(roundNo) {
        done = false
        delay(400)
        speaker.say("Count the ${item.plural}!")
    }
    LaunchedEffect(done) { if (done) { delay(3200); roundNo++ } }

    BoxWithConstraints(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding()) {
        val w = constraints.maxWidth
        val h = constraints.maxHeight
        val itemPx = (w * 0.32f).toInt()

        spots.forEachIndexed { i, (fx, fy) ->
            val isTapped = i in tapped
            val order = tapped.indexOf(i)
            val scale by animateFloatAsState(if (isTapped) 1.15f else 1f, spring(dampingRatio = 0.35f), label = "c")
            Box(
                Modifier
                    .offset { IntOffset((w * fx - itemPx / 2).toInt(), (h * fy - itemPx / 2).toInt()) }
                    .size(with(androidx.compose.ui.platform.LocalDensity.current) { itemPx.toDp() })
                    .scale(scale)
                    .clickable(enabled = !isTapped && !done) {
                        tapped.add(i)
                        Tones.play(523.0 + tapped.size * 80, 150)
                        val count = tapped.size
                        if (count == n) {
                            speaker.say("${numberNames[count - 1]}!") {
                                speaker.say("${numberNames[n - 1]} ${if (n == 1) item.singular else item.plural}!") { speaker.say("Yay! You did it!") }
                            }
                            done = true
                        } else speaker.say("${numberNames[count - 1]}!")
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(item.icon), item.singular, tint = Color.Unspecified, modifier = Modifier.fillMaxSize().alpha(if (isTapped) 0.85f else 1f))
                if (isTapped) {
                    Box(
                        Modifier.align(Alignment.TopEnd).size(56.dp).clip(CircleShape).background(Palette.mint),
                        contentAlignment = Alignment.Center,
                    ) { Text("${order + 1}", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold) }
                }
            }
        }

        if (done) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Text("$n", color = Palette.sun, fontSize = 140.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 40.dp))
            }
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text("Count", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 16.dp))
        }
    }
}
