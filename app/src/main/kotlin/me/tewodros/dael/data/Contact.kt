package me.tewodros.dael.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** A person the child can "call". Media paths are files inside the app's private storage. */
data class Contact(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val emoji: String = "🙂",
    val photoPath: String? = null,
    val videoPaths: List<String> = emptyList(),
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("emoji", emoji)
        put("photo", photoPath ?: JSONObject.NULL)
        put("videos", JSONArray(videoPaths))
    }

    companion object {
        fun fromJson(o: JSONObject): Contact {
            val videos = o.optJSONArray("videos") ?: JSONArray()
            return Contact(
                id = o.getString("id"),
                name = o.getString("name"),
                emoji = o.optString("emoji", "🙂"),
                photoPath = if (o.isNull("photo")) null else o.getString("photo"),
                videoPaths = List(videos.length()) { videos.getString(it) },
            )
        }

        fun listFromJson(raw: String?): List<Contact> {
            if (raw.isNullOrBlank()) return emptyList()
            val arr = JSONArray(raw)
            return List(arr.length()) { fromJson(arr.getJSONObject(it)) }
        }

        fun listToJson(list: List<Contact>): String = JSONArray(list.map { it.toJson() }).toString()

        val defaults = listOf(
            Contact(id = "mom", name = "Mom", emoji = "👩"),
            Contact(id = "dad", name = "Dad", emoji = "👨"),
        )
    }
}
