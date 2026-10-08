package me.tewodros.dael.ui.parent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import me.tewodros.dael.ui.Avatars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import me.tewodros.dael.data.Contact
import me.tewodros.dael.data.Repo
import me.tewodros.dael.data.Settings
import me.tewodros.dael.ui.Face
import me.tewodros.dael.ui.Palette
import java.io.File

@Composable
fun ParentSettingsScreen(
    repo: Repo,
    contacts: List<Contact>,
    settings: Settings,
    onDone: () -> Unit,
    onExitApp: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().background(Palette.bg).statusBarsPadding().navigationBarsPadding()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Parent settings", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Button(onClick = onDone) { Text("Done") }
        }
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                SettingsCard {
                    var name by remember(settings.childName) { mutableStateOf(settings.childName) }
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; scope.launch { repo.saveSettings(settings.copy(childName = it)) } },
                        label = { Text("Child's name (used in greetings)") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ToggleRow("Kiosk mode (pin app, block home/back)", settings.kiosk) {
                        scope.launch { repo.saveSettings(settings.copy(kiosk = it)) }
                    }
                    ToggleRow("Surprise incoming calls", settings.incomingCalls) {
                        scope.launch { repo.saveSettings(settings.copy(incomingCalls = it)) }
                    }
                    var minutes by remember(settings.incomingEveryMinutes) { mutableStateOf(settings.incomingEveryMinutes.toFloat()) }
                    Text("Call roughly every ${minutes.toInt()} min", color = Color.White)
                    Slider(
                        value = minutes,
                        onValueChange = { minutes = it },
                        onValueChangeFinished = { scope.launch { repo.saveSettings(settings.copy(incomingEveryMinutes = minutes.toInt())) } },
                        valueRange = 1f..15f,
                        steps = 13,
                    )
                }
            }
            item {
                Text("Family contacts", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Add a photo and record one or more short videos per person. The video plays when your child \"calls\" them.",
                    color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp,
                )
            }
            items(contacts, key = { it.id }) { c -> ContactEditor(repo, c) }
            item {
                OutlinedButton(onClick = { scope.launch { repo.upsert(Contact(name = "New person")) } }, modifier = Modifier.fillMaxWidth()) {
                    Text("+ Add person")
                }
            }
            item {
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onExitApp) { Text("Unpin and exit app", color = Palette.red) }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Palette.surface), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ContactEditor(repo: Repo, contact: Contact) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember(contact.id) { mutableStateOf(contact.name) }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val path = repo.importUri(uri, "jpg") ?: return@launch
            contact.photoPath?.let { File(it).delete() }
            repo.upsert(contact.copy(photoPath = path))
        }
    }
    val pickVideo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val path = repo.importUri(uri, "mp4") ?: return@launch
            repo.upsert(contact.copy(videoPaths = contact.videoPaths + path))
        }
    }
    var pendingVideo by remember { mutableStateOf<File?>(null) }
    val recordVideo = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { ok ->
        val f = pendingVideo
        pendingVideo = null
        if (ok && f != null && f.exists()) {
            scope.launch { repo.upsert(contact.copy(videoPaths = contact.videoPaths + f.absolutePath)) }
        } else f?.delete()
    }

    SettingsCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Face(contact, 64.dp)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; scope.launch { repo.upsert(contact.copy(name = it)) } },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Avatar", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            Avatars.keys.forEach { key ->
                Icon(
                    painterResource(Avatars.drawable(key)),
                    contentDescription = key,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(if (contact.avatar == key) 2.dp else 0.dp, Palette.coral, CircleShape)
                        .clickable { scope.launch { repo.upsert(contact.copy(avatar = key)) } },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text("Photo") }
            OutlinedButton(onClick = {
                val f = repo.newMediaFile("mp4")
                pendingVideo = f
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.files", f)
                recordVideo.launch(uri)
            }) { Text("Record video") }
            OutlinedButton(onClick = { pickVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }) { Text("Pick video") }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("${contact.videoPaths.size} video(s)", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            Row {
                if (contact.videoPaths.isNotEmpty()) {
                    TextButton(onClick = {
                        scope.launch {
                            contact.videoPaths.forEach { File(it).delete() }
                            repo.upsert(contact.copy(videoPaths = emptyList()))
                        }
                    }) { Text("Clear videos") }
                }
                TextButton(onClick = { scope.launch { repo.delete(contact) } }) { Text("Remove", color = Palette.red) }
            }
        }
    }
}
