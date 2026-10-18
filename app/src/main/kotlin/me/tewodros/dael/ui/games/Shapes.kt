package me.tewodros.dael.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

enum class Shape(val label: String) { CIRCLE("Circle"), SQUARE("Square"), TRIANGLE("Triangle"), STAR("Star"), HEART("Heart") }

private class Piece(val shape: Shape, val color: Color, val home: Offset) {
    var pos by mutableStateOf(home)
    var placed by mutableStateOf(false)
}

private class Round(val targets: List<Pair<Shape, Offset>>, val pieces: List<Piece>)

/**
 * Shape sorter: drag each colored shape onto its dashed outline. Three shapes per round, snapping
 * when close, with the shape's name spoken on success and a cheer when the round is done.
 */
@Composable
fun ShapesScreen(onBack: () -> Unit) {
    val speaker = LocalSpeaker.current
    val density = LocalDensity.current
    var roundNo by remember { mutableStateOf(0) }
    var celebrating by remember { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding()) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val n = 3
        val pieceSize = minOf(w / n * 0.78f, 420f)

        val round = remember(roundNo, w, h) {
            val shapes = Shape.entries.shuffled().take(n)
            val targetOrder = shapes.shuffled()
            val colors = Palette.tiles.shuffled()
            val targets = targetOrder.mapIndexed { i, s -> s to Offset(w * (i + 0.5f) / n, h * 0.30f) }
            val pieces = shapes.mapIndexed { i, s -> Piece(s, colors[i], Offset(w * (i + 0.5f) / n, h * 0.76f)) }
            Round(targets, pieces)
        }

        LaunchedEffect(celebrating) {
            if (celebrating) {
                delay(2200)
                celebrating = false
                roundNo++
            }
        }

        Canvas(Modifier.fillMaxSize()) {
            for ((shape, c) in round.targets) {
                drawShape(shape, c, pieceSize * 1.08f, Color.White.copy(alpha = 0.5f),
                    Stroke(8f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(28f, 20f))))
            }
        }

        round.pieces.forEach { piece ->
            val scale by animateFloatAsState(if (piece.placed) 1.08f else 1f, spring(dampingRatio = 0.4f), label = "snap")
            val box = (pieceSize * 1.3f).toInt()
            // Each piece is its own box positioned at its centre, so drags only start on the piece itself.
            Box(
                Modifier
                    .offset { IntOffset((piece.pos.x - box / 2f).toInt(), (piece.pos.y - box / 2f).toInt()) }
                    .size(with(density) { box.toDp() })
                    .zIndex(if (piece.placed) 0f else 1f)
                    .pointerInput(piece, round) {
                        if (piece.placed) return@pointerInput
                        detectDragGestures(
                            onDrag = { change, drag ->
                                change.consume()
                                piece.pos = Offset(piece.pos.x + drag.x, piece.pos.y + drag.y)
                            },
                            onDragEnd = {
                                val target = round.targets.first { it.first == piece.shape }.second
                                if (hypot(piece.pos.x - target.x, piece.pos.y - target.y) < pieceSize * 0.5f) {
                                    piece.pos = target
                                    piece.placed = true
                                    Tones.play(880.0, 160)
                                    if (round.pieces.all { it.placed }) {
                                        speaker.say("${piece.shape.label}!") { speaker.say("Yay! You did it!") }
                                        celebrating = true
                                    } else speaker.say("${piece.shape.label}!")
                                } else {
                                    Tones.play(300.0, 160)
                                    piece.pos = piece.home
                                }
                            },
                            onDragCancel = { piece.pos = piece.home },
                        )
                    }
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    drawShape(piece.shape, Offset(size.width / 2f, size.height / 2f), pieceSize * scale, piece.color, Fill)
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

private fun DrawScope.drawShape(shape: Shape, c: Offset, size: Float, color: Color, style: DrawStyle) {
    val r = size / 2f
    when (shape) {
        Shape.CIRCLE -> drawCircle(color, r, c, style = style)
        Shape.SQUARE -> drawRoundRect(color, Offset(c.x - r, c.y - r), Size(size, size), CornerRadius(size * 0.12f), style = style)
        Shape.TRIANGLE -> drawPath(Path().apply {
            moveTo(c.x, c.y - r); lineTo(c.x + r, c.y + r * 0.85f); lineTo(c.x - r, c.y + r * 0.85f); close()
        }, color, style = style)
        Shape.STAR -> drawPath(Path().apply {
            for (i in 0 until 10) {
                val a = -PI / 2 + i * PI / 5
                val rr = if (i % 2 == 0) r else r * 0.45f
                val x = (c.x + rr * cos(a)).toFloat(); val y = (c.y + rr * sin(a)).toFloat()
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }, color, style = style)
        Shape.HEART -> drawPath(Path().apply {
            val s = r * 0.9f
            moveTo(c.x, c.y + s * 0.9f)
            cubicTo(c.x - s * 1.6f, c.y - s * 0.1f, c.x - s * 0.9f, c.y - s * 1.1f, c.x, c.y - s * 0.4f)
            cubicTo(c.x + s * 0.9f, c.y - s * 1.1f, c.x + s * 1.6f, c.y - s * 0.1f, c.x, c.y + s * 0.9f)
            close()
        }, color, style = style)
    }
}
