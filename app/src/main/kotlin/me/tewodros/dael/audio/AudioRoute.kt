package me.tewodros.dael.audio

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import androidx.media3.exoplayer.ExoPlayer

/**
 * Forces playback to the built-in speaker when enabled, so a child holding the phone hears the app
 * even if a parent's earbuds are still connected.
 */
object AudioRoute {
    @Volatile var forceSpeaker: Boolean = true

    fun speaker(context: Context): AudioDeviceInfo? {
        if (!forceSpeaker) return null
        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
    }

    fun apply(context: Context, player: MediaPlayer) {
        speaker(context)?.let { runCatching { player.setPreferredDevice(it) } }
    }

    fun apply(context: Context, track: AudioTrack) {
        speaker(context)?.let { runCatching { track.setPreferredDevice(it) } }
    }

    fun apply(context: Context, player: ExoPlayer) {
        speaker(context)?.let { runCatching { player.setPreferredAudioDevice(it) } }
    }
}
