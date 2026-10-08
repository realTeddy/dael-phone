package me.tewodros.dael.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.Palette
import kotlin.math.hypot
import kotlin.random.Random

private data class Hue(val name: String, val color: Color)

private val hues = listOf(
    Hue("Red", Color(0xFFEF4444)), Hue("Blue", Color(0xFF3B82F6)), Hue("Green", Color(0xFF22C55E)),
    Hue("Yellow", Color(0xFFFACC15)), Hue("Purple", Color(0xFFA855F7)), Hue("Pink", Color(0xFFF472B6)),
    Hue("Orange", Color(0xFFF97316)),
)

private class Bubble(var x: Float, var y: Float, val r: Float, val hue: Hue, val vy: Float, val wobble: Float)
private class Pop(val x: Float, val y: Float, val color: Color, var age: Float = 0f)

/** Bubbles drift up the screen; tapping one pops it and says its color. */
@Composable
fun BubblesScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    val bubbles = remember { mutableStateListOf<Bubble>() }
    val pops = remember { mutableStateListOf<Pop>() }
    var size by remember { mutableStateOf(Offset.Zero) }
    var tick by remember { mutableStateOf(0L) }

    fun spawn(w: Float, h: Float, fromBottom: Boolean = true) {
        val r = Random.nextInt(110, 170).toFloat()
        bubbles.add(
            Bubble(
                x = Random.nextFloat() * (w - 2 * r) + r,
                y = if (fromBottom) h + r else Random.nextFloat() * h,
                r = r, hue = hues.random(),
                vy = Random.nextFloat() * 60f + 50f,
                wobble = Random.nextFloat() * 6.28f,
            )
        )
    }

    LaunchedEffect(size) {
        if (size == Offset.Zero) return@LaunchedEffect
        if (bubbles.isEmpty()) repeat(6) { spawn(size.x, size.y, fromBottom = false) }
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) 0f else (now - last) / 1_000_000_000f
                last = now
                val t = now / 1_000_000_000f
                for (b in bubbles) {
                    b.y -= b.vy * dt
                    b.x += kotlin.math.sin(t * 2f + b.wobble) * 20f * dt
                }
                bubbles.removeAll { it.y < -it.r }
                for (p in pops) p.age += dt
                pops.removeAll { it.age > 0.35f }
                while (bubbles.size < 6) spawn(size.x, size.y)
                tick = now
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding()) {
        Canvas(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { p ->
                        val hit = bubbles.lastOrNull { hypot(it.x - p.x, it.y - p.y) <= it.r * 1.3f } ?: return@detectTapGestures
                        bubbles.remove(hit)
                        pops.add(Pop(hit.x, hit.y, hit.hue.color))
                        Tones.play(1400.0, 90)
                        speaker.say("${hit.hue.name}!")
                    }
                }
        ) {
            @Suppress("UNUSED_EXPRESSION") tick
            if (size != Offset(this.size.width, this.size.height)) size = Offset(this.size.width, this.size.height)
            for (b in bubbles) {
                drawCircle(b.hue.color.copy(alpha = 0.85f), b.r, Offset(b.x, b.y))
                drawCircle(Color.White.copy(alpha = 0.35f), b.r, Offset(b.x, b.y), style = androidx.compose.ui.graphics.drawscope.Stroke(6f))
                drawCircle(Color.White.copy(alpha = 0.8f), b.r * 0.18f, Offset(b.x - b.r * 0.35f, b.y - b.r * 0.4f))
            }
            for (p in pops) {
                val k = p.age / 0.35f
                drawCircle(p.color.copy(alpha = 1f - k), 40f + 160f * k, Offset(p.x, p.y), style = androidx.compose.ui.graphics.drawscope.Stroke(10f * (1f - k) + 1f))
            }
        }
        BackButton(onBack, Modifier.padding(16.dp))
    }
}
