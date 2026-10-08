package me.tewodros.dael.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** A person the child can "call". Media paths are files inside the app's private storage. */
data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatar: String = "woman",
    val photoPath: String? = null,
    val videoPaths: List<String> = emptyList(),
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("avatar", avatar)
        put("photo", photoPath ?: JSONObject.NULL)
        put("videos", JSONArray(videoPaths))
    }

    companion object {
        fun fromJson(o: JSONObject): Contact {
            val videos = o.optJSONArray("videos") ?: JSONArray()
            return Contact(
                id = o.getString("id"),
                name = o.getString("name"),
                avatar = o.optString("avatar", legacyAvatar(o.optString("emoji", ""))),
                photoPath = if (o.isNull("photo")) null else o.getString("photo"),
                videoPaths = List(videos.length()) { videos.getString(it) },
            )
        }

        /** Contacts saved by early builds stored an emoji instead of an avatar key. */
        private fun legacyAvatar(emoji: String): String = when (emoji) {
            "👨", "👴" -> if (emoji == "👴") "grandpa" else "man"
            "👵" -> "grandma"
            "👦" -> "boy"
            "👧" -> "girl"
            else -> "woman"
        }

        fun listFromJson(raw: String?): List<Contact> {
            if (raw.isNullOrBlank()) return emptyList()
            val arr = JSONArray(raw)
            return List(arr.length()) { fromJson(arr.getJSONObject(it)) }
        }

        fun listToJson(list: List<Contact>): String = JSONArray(list.map { it.toJson() }).toString()

        val defaults = listOf(
            Contact(id = "mom", name = "Mom", avatar = "woman"),
            Contact(id = "dad", name = "Dad", avatar = "man"),
        )
    }
}
