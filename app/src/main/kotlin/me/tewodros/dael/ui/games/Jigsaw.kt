package me.tewodros.dael.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import me.tewodros.dael.audio.LocalSpeaker
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.Palette
import kotlin.math.hypot

private class Tile(val row: Int, val col: Int, val home: Offset) {
    var pos by mutableStateOf(home)
    var placed by mutableStateOf(false)
}

/**
 * Four-piece picture puzzle. An animal picture is cut into quarters; the child drags each quarter
 * into the faint full picture at the top. Pieces snap when close. Finishing cheers and loads the next animal.
 */
@Composable
fun JigsawScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    val density = LocalDensity.current
    var roundNo by remember { mutableStateOf(0) }
    var celebrating by remember { mutableStateOf(false) }
    val animal = remember(roundNo) { farmAnimals[roundNo % farmAnimals.size] }
    val painter: Painter = painterResource(animal.icon)

    BoxWithConstraints(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val pic = minOf(w * 0.62f, h * 0.33f)       // full picture side
        val cell = pic / 2f                          // one piece side
        val frame = Offset((w - pic) / 2f, h * 0.08f) // top-left of the frame

        val tiles = remember(roundNo, w, h) {
            val slots = listOf(0 to 0, 0 to 1, 1 to 0, 1 to 1)
            // Pieces wait in a loose two-by-two block below the frame, in shuffled order.
            val spots = listOf(Offset(w * 0.3f, h * 0.62f), Offset(w * 0.7f, h * 0.62f), Offset(w * 0.3f, h * 0.84f), Offset(w * 0.7f, h * 0.84f)).shuffled()
            val homes = slots.indices.map { spots[it] }
            slots.mapIndexed { i, (r, c) -> Tile(r, c, homes[i]) }
        }

        LaunchedEffect(celebrating) {
            if (celebrating) { delay(2400); celebrating = false; roundNo++ }
        }

        // Faint target picture plus dashed cut lines.
        Canvas(Modifier.fillMaxSize()) {
            translate(frame.x, frame.y) {
                with(painter) { draw(Size(pic, pic), alpha = 0.18f) }
            }
            val dash = Stroke(6f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 18f)))
            drawRect(Color.White.copy(alpha = 0.5f), frame, Size(pic, pic), style = dash)
            drawLine(Color.White.copy(alpha = 0.5f), Offset(frame.x + cell, frame.y), Offset(frame.x + cell, frame.y + pic), 6f, pathEffect = dash.pathEffect)
            drawLine(Color.White.copy(alpha = 0.5f), Offset(frame.x, frame.y + cell), Offset(frame.x + pic, frame.y + cell), 6f, pathEffect = dash.pathEffect)
        }

        tiles.forEach { tile ->
            val box = (cell * 1.25f).toInt()
            val targetCenter = Offset(frame.x + cell * (tile.col + 0.5f), frame.y + cell * (tile.row + 0.5f))
            Box(
                Modifier
                    .offset { IntOffset((tile.pos.x - box / 2f).toInt(), (tile.pos.y - box / 2f).toInt()) }
                    .size(with(density) { box.toDp() })
                    .zIndex(if (tile.placed) 0f else 1f)
                    .pointerInput(tile, tiles) {
                        if (tile.placed) return@pointerInput
                        detectDragGestures(
                            onDrag = { change, drag ->
                                change.consume()
                                tile.pos = Offset(tile.pos.x + drag.x, tile.pos.y + drag.y)
                            },
                            onDragEnd = {
                                if (hypot(tile.pos.x - targetCenter.x, tile.pos.y - targetCenter.y) < cell * 0.45f) {
                                    tile.pos = targetCenter
                                    tile.placed = true
                                    Tones.play(880.0, 160)
                                    if (tiles.all { it.placed }) {
                                        speaker.sayThenSound("${animal.name}!", animal.key) { speaker.say("Yay! You did it!") }
                                        celebrating = true
                                    }
                                } else {
                                    Tones.play(300.0, 160)
                                    tile.pos = tile.home
                                }
                            },
                            onDragCancel = { tile.pos = tile.home },
                        )
                    }
            ) {
                // Draw just this quarter of the picture, on a card so the piece reads as a piece.
                Canvas(Modifier.fillMaxSize()) {
                    val left = (size.width - cell) / 2f
                    val top = (size.height - cell) / 2f
                    drawRoundRect(Palette.surface, Offset(left - 6f, top - 6f), Size(cell + 12f, cell + 12f), androidx.compose.ui.geometry.CornerRadius(18f))
                    clipRect(left, top, left + cell, top + cell) {
                        translate(left - cell * tile.col, top - cell * tile.row) {
                            with(painter) { draw(Size(pic, pic)) }
                        }
                    }
                }
            }
        }

        if (celebrating) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Yay!", color = Palette.sun, fontSize = 96.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        BackButton(onBack, Modifier.padding(16.dp))
    }
}
