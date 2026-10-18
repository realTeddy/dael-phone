package me.tewodros.dael.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.Palette
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private data class Hue(val name: String, val color: Color)

private val hues = listOf(
    Hue("Red", Color(0xFFEF4444)), Hue("Blue", Color(0xFF3B82F6)), Hue("Green", Color(0xFF22C55E)),
    Hue("Yellow", Color(0xFFFACC15)), Hue("Purple", Color(0xFFA855F7)), Hue("Pink", Color(0xFFF472B6)),
    Hue("Orange", Color(0xFFF97316)),
)

private val letterWords = mapOf(
    "A" to "Apple", "B" to "Ball", "C" to "Cat", "D" to "Dog", "E" to "Egg", "F" to "Fish", "G" to "Goat", "H" to "Hat",
    "I" to "Igloo", "J" to "Juice", "K" to "Kite", "L" to "Lion", "M" to "Moon", "N" to "Nose", "O" to "Orange",
    "P" to "Pig", "Q" to "Queen", "R" to "Rabbit", "S" to "Sun", "T" to "Train", "U" to "Umbrella", "V" to "Van",
    "W" to "Water", "X" to "Box", "Y" to "Yellow", "Z" to "Zebra",
)
private val numberWords = listOf("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten")

enum class BubbleMode { COLORS, LETTERS, NUMBERS }

/** What a bubble carries: a color name, an uppercase letter, or a count 1..10. */
private class Bubble(
    var x: Float, var y: Float, val r: Float, val hue: Hue, val vy: Float, val wobble: Float,
    val label: String, val count: Int, val phrase: String,
)

/** Short-lived pop effects: a ring, a letter that grows and fades, or N scattering mini bubbles. */
private class Pop(val x: Float, val y: Float, val color: Color, val label: String, val count: Int, var age: Float = 0f)

/**
 * Bubbles drift up; popping one says what it carries. Letters and numbers are served in order
 * (A, B, C… / 1, 2, 3…) so repeated play exposes the whole sequence, the way the alphabet song does.
 */
@Composable
fun BubblesScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    val measurer = rememberTextMeasurer()
    val bubbles = remember { mutableStateListOf<Bubble>() }
    val pops = remember { mutableStateListOf<Pop>() }
    var mode by remember { mutableStateOf(BubbleMode.COLORS) }
    var size by remember { mutableStateOf(Offset.Zero) }
    var tick by remember { mutableStateOf(0L) }
    var letterIdx by remember { mutableStateOf(0) }
    var numberIdx by remember { mutableStateOf(0) }

    fun spawn(w: Float, h: Float, fromBottom: Boolean = true) {
        val r = Random.nextInt(110, 170).toFloat()
        val hue = hues.random()
        val (label, count, phrase) = when (mode) {
            BubbleMode.COLORS -> Triple("", 0, "${hue.name}!")
            BubbleMode.LETTERS -> {
                val l = ('A' + letterIdx % 26).toString(); letterIdx++
                Triple(l, 0, "$l! ${letterWords[l]}!")
            }
            BubbleMode.NUMBERS -> {
                val n = numberIdx % 10 + 1; numberIdx++
                Triple(n.toString(), n, "${numberWords[n - 1]}!")
            }
        }
        bubbles.add(
            Bubble(
                x = Random.nextFloat() * (w - 2 * r) + r,
                y = if (fromBottom) h + r else Random.nextFloat() * h * 0.8f,
                r = r, hue = hue,
                vy = Random.nextFloat() * 50f + 40f,
                wobble = Random.nextFloat() * 6.28f,
                label = label, count = count, phrase = phrase,
            )
        )
    }

    LaunchedEffect(mode) {
        bubbles.clear(); pops.clear()
        if (size != Offset.Zero) repeat(5) { spawn(size.x, size.y, fromBottom = false) }
    }

    LaunchedEffect(size) {
        if (size == Offset.Zero) return@LaunchedEffect
        if (bubbles.isEmpty()) repeat(5) { spawn(size.x, size.y, fromBottom = false) }
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else (now - last) / 1_000_000_000f
                last = now
                val t = now / 1_000_000_000f
                for (b in bubbles) {
                    b.y -= b.vy * dt
                    b.x += sin(t * 2f + b.wobble) * 20f * dt
                }
                bubbles.removeAll { it.y < -it.r }
                for (p in pops) p.age += dt
                pops.removeAll { it.age > 1.1f }
                while (bubbles.size < 5) spawn(size.x, size.y)
                tick = now
            }
        }
    }

    Column(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding().navigationBarsPadding()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            Canvas(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { p ->
                            val hit = bubbles.lastOrNull { hypot(it.x - p.x, it.y - p.y) <= it.r * 1.3f } ?: return@detectTapGestures
                            bubbles.remove(hit)
                            pops.add(Pop(hit.x, hit.y, hit.hue.color, hit.label, hit.count))
                            Tones.play(1400.0, 90)
                            speaker.say(hit.phrase)
                        }
                    }
            ) {
                @Suppress("UNUSED_EXPRESSION") tick
                if (size != Offset(this.size.width, this.size.height)) size = Offset(this.size.width, this.size.height)
                for (b in bubbles) {
                    drawCircle(b.hue.color.copy(alpha = 0.85f), b.r, Offset(b.x, b.y))
                    drawCircle(Color.White.copy(alpha = 0.35f), b.r, Offset(b.x, b.y), style = Stroke(6f))
                    drawCircle(Color.White.copy(alpha = 0.8f), b.r * 0.18f, Offset(b.x - b.r * 0.35f, b.y - b.r * 0.4f))
                    if (b.label.isNotEmpty()) {
                        val style = TextStyle(color = Color.White, fontSize = (b.r * 0.42f).sp, fontWeight = FontWeight.ExtraBold)
                        val layout = measurer.measure(b.label, style)
                        val dy = if (b.count > 0) -b.r * 0.22f else 0f
                        drawText(layout, topLeft = Offset(b.x - layout.size.width / 2f, b.y - layout.size.height / 2f + dy))
                        if (b.count > 0) {
                            // Dots under the numeral: the quantity, not just the symbol.
                            val dotR = b.r * 0.07f
                            val perRow = if (b.count > 5) 5 else b.count
                            val rows = (b.count + 4) / 5
                            for (i in 0 until b.count) {
                                val row = i / 5; val col = i % 5
                                val inRow = if (row < rows - 1) 5 else b.count - 5 * (rows - 1)
                                val cx = b.x + (col - (inRow - 1) / 2f) * dotR * 2.6f
                                val cy = b.y + b.r * 0.32f + row * dotR * 2.6f
                                drawCircle(Color.White, dotR, Offset(cx, cy))
                            }
                            @Suppress("UNUSED_VARIABLE") val unused = perRow
                        }
                    }
                }
                for (p in pops) {
                    val k = (p.age / 0.35f).coerceAtMost(1f)
                    drawCircle(p.color.copy(alpha = 1f - k), 40f + 160f * k, Offset(p.x, p.y), style = Stroke(10f * (1f - k) + 1f))
                    val k2 = (p.age / 1.1f).coerceAtMost(1f)
                    if (p.label.isNotEmpty()) {
                        // The letter or numeral bursts out of the bubble, grows, and floats up.
                        val style = TextStyle(color = p.color.copy(alpha = 1f - k2), fontSize = (80f + 140f * k2).sp, fontWeight = FontWeight.ExtraBold)
                        val layout = measurer.measure(p.label, style)
                        drawText(layout, topLeft = Offset(p.x - layout.size.width / 2f, p.y - layout.size.height / 2f - 120f * k2))
                    }
                    if (p.count > 0) {
                        // N little bubbles scatter: one per unit, so "three" looks like three things.
                        for (i in 0 until p.count) {
                            val a = i * 6.283f / p.count
                            val d = 60f + 220f * k2
                            drawCircle(p.color.copy(alpha = 0.9f * (1f - k2)), 26f, Offset(p.x + cos(a) * d, p.y + sin(a) * d))
                        }
                    }
                }
            }
            BackButton(onBack, Modifier.padding(16.dp))
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            ModeButton("●", mode == BubbleMode.COLORS, Palette.coral) { mode = BubbleMode.COLORS }
            ModeButton("ABC", mode == BubbleMode.LETTERS, Palette.sky) { mode = BubbleMode.LETTERS }
            ModeButton("123", mode == BubbleMode.NUMBERS, Palette.mint) { mode = BubbleMode.NUMBERS }
        }
    }
}

@Composable
private fun ModeButton(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(if (selected) color else Palette.surface)
            .clickable { Tones.blip(); onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontSize = if (label.length > 1) 22.sp else 30.sp, fontWeight = FontWeight.ExtraBold)
    }
}
