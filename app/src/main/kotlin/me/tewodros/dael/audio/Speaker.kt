package me.tewodros.dael.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

/** Thin wrapper over Android text to speech. Speech requests before init are dropped silently. */
class Speaker(context: Context) {
    private var ready = false
    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) {
                tts.language = Locale.getDefault()
                tts.setSpeechRate(0.85f)
                tts.setPitch(1.1f)
            }
        }
    }

    fun say(text: String, interrupt: Boolean = true) {
        if (!ready) return
        val mode = if (interrupt) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        tts.speak(text, mode, null, text.hashCode().toString())
    }

    fun stop() {
        if (ready) tts.stop()
    }

    fun shutdown() = tts.shutdown()
}

val LocalSpeaker = staticCompositionLocalOf<Speaker> { error("Speaker not provided") }
