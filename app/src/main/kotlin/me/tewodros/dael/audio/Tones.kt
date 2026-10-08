package me.tewodros.dael.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Tiny synth that plays a decaying sine note. Used by the piano and as tap feedback. */
object Tones {
    private const val SAMPLE_RATE = 22_050
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun play(frequencyHz: Double, durationMs: Int = 450) {
        scope.launch {
            val count = SAMPLE_RATE * durationMs / 1000
            val pcm = ShortArray(count)
            for (i in 0 until count) {
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = exp(-4.0 * t / (durationMs / 1000.0))
                // A little second harmonic makes it sound less like a test tone.
                val sample = sin(2 * PI * frequencyHz * t) * 0.8 + sin(4 * PI * frequencyHz * t) * 0.2
                pcm[i] = (sample * envelope * Short.MAX_VALUE * 0.6).toInt().toShort()
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            track.write(pcm, 0, pcm.size)
            track.setNotificationMarkerPosition(pcm.size)
            track.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(t: AudioTrack) { t.release() }
                override fun onPeriodicNotification(t: AudioTrack) {}
            })
            track.play()
        }
    }

    /** Short bright blip for buttons. */
    fun blip() = play(880.0, 120)

    /** Classic two-tone ring. */
    fun ring() {
        play(660.0, 300)
        scope.launch { kotlinx.coroutines.delay(320); play(880.0, 300) }
    }
}
