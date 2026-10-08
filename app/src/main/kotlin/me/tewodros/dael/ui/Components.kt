package me.tewodros.dael.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import me.tewodros.dael.audio.Tones
import me.tewodros.dael.data.Contact
import java.io.File

/** Big, squishy, colorful button. Every tap gets a sound and a bounce. */
@Composable
fun BigTile(
    label: String,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, spring(dampingRatio = 0.4f), label = "tile")
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clip(RoundedCornerShape(28.dp))
            .background(color)
            .clickable(interactionSource = interaction, indication = null) {
                Tones.blip()
                onClick()
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(emoji, fontSize = 56.sp)
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
fun RoundButton(
    color: Color,
    size: androidx.compose.ui.unit.Dp = 88.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
fun BackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    RoundButton(Palette.surface, size = 64.dp, modifier = modifier, onClick = { Tones.blip(); onBack() }) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(36.dp))
    }
}

/** Contact face: a photo if the parent set one, otherwise a big emoji. */
@Composable
fun Face(contact: Contact, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size).clip(CircleShape).background(Palette.surface),
        contentAlignment = Alignment.Center,
    ) {
        val photo = contact.photoPath?.let(::File)?.takeIf { it.exists() }
        if (photo != null) {
            AsyncImage(model = photo, contentDescription = contact.name, modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        } else {
            Text(contact.emoji, fontSize = (size.value * 0.55f).sp)
        }
    }
}
