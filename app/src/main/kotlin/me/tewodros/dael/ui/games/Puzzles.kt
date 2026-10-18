package me.tewodros.dael.ui.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tewodros.dael.R
import me.tewodros.dael.ui.BackButton
import me.tewodros.dael.ui.BigTile
import me.tewodros.dael.ui.Palette
import me.tewodros.dael.ui.Routes

/** Hub for the puzzle games so the home screen stays at eight tiles. */
@Composable
fun PuzzlesScreen(onBack: () -> Unit, onOpen: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding().navigationBarsPadding().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackButton(onBack)
            Text("Puzzles", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(start = 16.dp))
        }
        val games = listOf(
            Triple("Shapes", R.drawable.ic_tile_shapes, Routes.SHAPES),
            Triple("Find it", R.drawable.ic_tile_match, Routes.MATCH),
            Triple("Picture", R.drawable.ic_tile_jigsaw, Routes.JIGSAW),
            Triple("Colors", R.drawable.ic_tile_colors, Routes.COLORS),
            Triple("Count", R.drawable.ic_tile_count, Routes.COUNT),
            Triple("Who is it?", R.drawable.ic_tile_sound, Routes.SOUND),
        )
        Column(Modifier.fillMaxSize().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            games.chunked(2).forEachIndexed { r, pair ->
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    pair.forEachIndexed { c, (label, icon, route) ->
                        BigTile(label, icon, Palette.tiles[(r * 2 + c + 1) % Palette.tiles.size], Modifier.weight(1f).fillMaxSize()) { onOpen(route) }
                    }
                }
            }
        }
    }
}
