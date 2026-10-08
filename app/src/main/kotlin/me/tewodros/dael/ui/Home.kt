package me.tewodros.dael.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.tewodros.dael.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeApp(val route: String, val label: String, val icon: Int)

val homeApps = listOf(
    HomeApp(Routes.PHONE, "Phone", R.drawable.ic_tile_phone),
    HomeApp(Routes.BUBBLES, "Bubbles", R.drawable.ic_tile_bubbles),
    HomeApp(Routes.ANIMALS, "Animals", R.drawable.ic_tile_animals),
    HomeApp(Routes.PIANO, "Piano", R.drawable.ic_tile_piano),
    HomeApp(Routes.PAINT, "Paint", R.drawable.ic_tile_paint),
    HomeApp(Routes.PEEKABOO, "Peekaboo", R.drawable.ic_tile_peekaboo),
)

@Composable
fun HomeScreen(onOpen: (String) -> Unit, onParentGate: () -> Unit) {
    var clock by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            clock = SimpleDateFormat("h:mm", Locale.getDefault()).format(Date())
            delay(10_000)
        }
    }
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize().background(Palette.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(clock, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(painterResource(R.drawable.ic_signal), null, tint = Color.Unspecified, modifier = Modifier.size(22.dp))
                    Icon(painterResource(R.drawable.ic_battery), null, tint = Color.Unspecified, modifier = Modifier.size(26.dp))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp)) {
                Text("Hi!", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.width(10.dp))
                Icon(painterResource(R.drawable.ic_hand_wave), null, tint = Color.Unspecified, modifier = Modifier.size(44.dp))
            }
            // Plain rows instead of a lazy grid: six fixed tiles need no recycling,
            // and fewer moving parts means fewer ways to end up with a blank screen.
            Column(
                Modifier.fillMaxSize().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                homeApps.chunked(2).forEachIndexed { rowIdx, pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        pair.forEachIndexed { colIdx, app ->
                            val idx = rowIdx * 2 + colIdx
                            BigTile(app.label, app.icon, Palette.tiles[idx % Palette.tiles.size], Modifier.weight(1f)) { onOpen(app.route) }
                        }
                    }
                }
            }
        }

        // Invisible parent gate: hold the top-right corner for three seconds.
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .size(72.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onPress = {
                        val job = scope.launch {
                            delay(3000)
                            onParentGate()
                        }
                        tryAwaitRelease()
                        job.cancel()
                    })
                }
        )
    }
}
