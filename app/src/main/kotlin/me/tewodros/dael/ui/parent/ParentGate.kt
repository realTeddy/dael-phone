package me.tewodros.dael.ui.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.tewodros.dael.ui.Palette

/** A toddler-proof gate: multiply two numbers. Small text on purpose. */
@Composable
fun ParentGateScreen(onPass: () -> Unit, onCancel: () -> Unit) {
    val a = remember { (3..9).random() }
    val b = remember { (3..9).random() }
    val answer = a * b
    val options = remember { (setOf(answer) + generateSequence { (9..81).random() }.filter { it != answer }.take(2).toSet()).shuffled() }

    Column(
        Modifier.fillMaxSize().background(Palette.bg).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Parents only", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Text("What is $a × $b?", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { o ->
                Button(onClick = { if (o == answer) onPass() else onCancel() }) { Text("$o", fontSize = 14.sp) }
            }
        }
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onCancel) { Text("Back", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp) }
    }
}
