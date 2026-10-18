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
        Column(Modifier.fillMaxSize().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            BigTile("Shapes", R.drawable.ic_tile_shapes, Palette.sun, Modifier.weight(1f).fillMaxWidth()) { onOpen(Routes.SHAPES) }
            BigTile("Find it", R.drawable.ic_tile_match, Palette.mint, Modifier.weight(1f).fillMaxWidth()) { onOpen(Routes.MATCH) }
            BigTile("Picture", R.drawable.ic_tile_jigsaw, Palette.grape, Modifier.weight(1f).fillMaxWidth()) { onOpen(Routes.JIGSAW) }
        }
    }
}
