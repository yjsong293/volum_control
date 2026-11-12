package com.example.volumelock

import android.content.Context
import android.media.AudioManager

object VolumePrefs {
    private const val PREFS = "volume_lock_settings"
    private const val KEY_ENABLED = "enabled"

    data class StreamSetting(
        val key: String,
        val title: String,
        val stream: Int,
        val defaultPercent: Int
    )

    val streams = listOf(
        StreamSetting("music", "미디어", AudioManager.STREAM_MUSIC, 50),
        StreamSetting("call", "통화", AudioManager.STREAM_VOICE_CALL, 70),
        StreamSetting("ring", "벨소리", AudioManager.STREAM_RING, 30),
        StreamSetting("notification", "알림", AudioManager.STREAM_NOTIFICATION, 40),
        StreamSetting("alarm", "알람", AudioManager.STREAM_ALARM, 60),
        StreamSetting("system", "시스템", AudioManager.STREAM_SYSTEM, 30)
    )

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context) = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    fun isStreamEnabled(context: Context, key: String) =
        prefs(context).getBoolean("${key}_enabled", false)

    fun setStreamEnabled(context: Context, key: String, enabled: Boolean) {
        prefs(context).edit().putBoolean("${key}_enabled", enabled).apply()
    }

    fun getPercent(context: Context, setting: StreamSetting) =
        prefs(context).getInt("${setting.key}_percent", setting.defaultPercent)

    fun setPercent(context: Context, key: String, percent: Int) {
        prefs(context).edit().putInt("${key}_percent", percent.coerceIn(0, 100)).apply()
    }
}
