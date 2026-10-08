package me.tewodros.dael.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.Palette
import me.tewodros.dael.ui.RoundButton

private class Stroke2(val color: Color, val points: MutableList<Offset>)

@Composable
fun PaintScreen(onBack: () -> Unit) {
    val strokes = remember { mutableStateListOf<Stroke2>() }
    var color by remember { mutableStateOf(Palette.coral) }
    var version by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            BackButton(onBack)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Palette.tiles.forEach { c ->
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(if (color == c) 4.dp else 0.dp, Color.White, CircleShape)
                            .clickable { Tones.blip(); color = c }
                    )
                }
            }
            RoundButton(Palette.surface, size = 56.dp, onClick = { Tones.blip(); strokes.clear(); version++ }) {
                Icon(Icons.Filled.Delete, contentDescription = "Clear", tint = Color.White)
            }
        }
        Canvas(
            Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { strokes.add(Stroke2(color, mutableListOf(it))); version++ },
                        onDrag = { change, _ ->
                            strokes.lastOrNull()?.points?.add(change.position)
                            version++
                        },
                    )
                }
        ) {
            @Suppress("UNUSED_EXPRESSION") version
            strokes.forEach { s ->
                if (s.points.size == 1) {
                    drawCircle(s.color, radius = 14f, center = s.points[0])
                } else {
                    val path = Path().apply {
                        moveTo(s.points[0].x, s.points[0].y)
                        s.points.drop(1).forEach { lineTo(it.x, it.y) }
                    }
                    drawPath(path, s.color, style = Stroke(width = 28f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }
        }
    }
}
