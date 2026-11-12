package com.example.volumelock

import android.content.Context
import android.media.AudioManager

object VolumeApplier {
    fun apply(context: Context) {
        if (!VolumePrefs.isEnabled(context)) return
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        VolumePrefs.streams.forEach { setting ->
            if (VolumePrefs.isStreamEnabled(context, setting.key)) {
                try {
                    val max = audio.getStreamMaxVolume(setting.stream)
                    if (max > 0) {
                        val percent = VolumePrefs.getPercent(context, setting)
                        val target = ((max * percent) / 100.0).toInt().coerceIn(0, max)
                        val current = audio.getStreamVolume(setting.stream)
                        if (current != target) {
                            audio.setStreamVolume(setting.stream, target, 0)
                        }
                    }
                } catch (_: SecurityException) {
                    // Some devices or audio states may restrict a stream.
                } catch (_: IllegalArgumentException) {
                    // A stream may not be supported by a particular device.
                }
            }
        }
    }
}
