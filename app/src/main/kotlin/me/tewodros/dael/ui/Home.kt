package me.tewodros.dael.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeApp(val route: String, val label: String, val emoji: String)

val homeApps = listOf(
    HomeApp(Routes.DIALER, "Phone", "📞"),
    HomeApp(Routes.CONTACTS, "Family", "👨‍👩‍👦"),
    HomeApp(Routes.ANIMALS, "Animals", "🐶"),
    HomeApp(Routes.PIANO, "Piano", "🎹"),
    HomeApp(Routes.PAINT, "Paint", "🎨"),
    HomeApp(Routes.PEEKABOO, "Peekaboo", "🙈"),
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
                Text("📶 🔋", color = Color.White, fontSize = 18.sp)
            }
            Text(
                "Hi! 👋",
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(homeApps) { app ->
                    val idx = homeApps.indexOf(app)
                    BigTile(app.label, app.emoji, Palette.tiles[idx % Palette.tiles.size]) { onOpen(app.route) }
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
