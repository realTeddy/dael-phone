package me.tewodros.dael.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dael")

data class Settings(
    val kiosk: Boolean = false,
    val incomingCalls: Boolean = true,
    /** Minimum minutes between surprise incoming calls. */
    val incomingEveryMinutes: Int = 3,
    val childName: String = "Dael",
)

class Repo(private val context: Context) {
    private object Keys {
        val CONTACTS = stringPreferencesKey("contacts")
        val KIOSK = booleanPreferencesKey("kiosk")
        val INCOMING = booleanPreferencesKey("incoming")
        val INCOMING_MIN = intPreferencesKey("incoming_min")
        val CHILD_NAME = stringPreferencesKey("child_name")
    }

    val contacts: Flow<List<Contact>> = context.dataStore.data.map { p ->
        val raw = p[Keys.CONTACTS]
        if (raw == null) Contact.defaults else Contact.listFromJson(raw)
    }

    val settings: Flow<Settings> = context.dataStore.data.map { p ->
        Settings(
            kiosk = p[Keys.KIOSK] ?: false,
            incomingCalls = p[Keys.INCOMING] ?: true,
            incomingEveryMinutes = p[Keys.INCOMING_MIN] ?: 3,
            childName = p[Keys.CHILD_NAME] ?: "Dael",
        )
    }

    suspend fun saveContacts(list: List<Contact>) {
        context.dataStore.edit { it[Keys.CONTACTS] = Contact.listToJson(list) }
    }

    suspend fun upsert(contact: Contact) {
        context.dataStore.edit { p ->
            val current = p[Keys.CONTACTS]?.let { Contact.listFromJson(it) } ?: Contact.defaults
            val next = if (current.any { it.id == contact.id }) {
                current.map { if (it.id == contact.id) contact else it }
            } else current + contact
            p[Keys.CONTACTS] = Contact.listToJson(next)
        }
    }

    suspend fun delete(contact: Contact) {
        context.dataStore.edit { p ->
            val current = p[Keys.CONTACTS]?.let { Contact.listFromJson(it) } ?: Contact.defaults
            p[Keys.CONTACTS] = Contact.listToJson(current.filterNot { it.id == contact.id })
        }
        contact.photoPath?.let { File(it).delete() }
        contact.videoPaths.forEach { File(it).delete() }
    }

    suspend fun saveSettings(s: Settings) {
        context.dataStore.edit {
            it[Keys.KIOSK] = s.kiosk
            it[Keys.INCOMING] = s.incomingCalls
            it[Keys.INCOMING_MIN] = s.incomingEveryMinutes
            it[Keys.CHILD_NAME] = s.childName
        }
    }

    /** Directory shared with the FileProvider so the system camera can write straight into it. */
    fun mediaDir(): File = File(context.filesDir, "media").apply { mkdirs() }

    fun newMediaFile(ext: String): File = File(mediaDir(), "${UUID.randomUUID()}.$ext")

    /** Copies a picked content:// URI into private storage so it survives permission expiry. */
    suspend fun importUri(uri: Uri, ext: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val target = newMediaFile(ext)
            context.contentResolver.openInputStream(uri)!!.use { input ->
                target.outputStream().use { input.copyTo(it) }
            }
            target.absolutePath
        }.getOrNull()
    }
}
