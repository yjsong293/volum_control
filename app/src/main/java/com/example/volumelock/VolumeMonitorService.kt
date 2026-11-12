package com.example.volumelock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.database.ContentObserver
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings

class VolumeMonitorService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var observer: ContentObserver? = null
    private var applying = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                if (applying || !VolumePrefs.isEnabled(this@VolumeMonitorService)) return
                handler.removeCallbacks(reapplyRunnable)
                handler.postDelayed(reapplyRunnable, 250)
            }
        }
        contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            observer!!
        )
        VolumeApplier.apply(this)
    }

    private val reapplyRunnable = Runnable {
        applying = true
        VolumeApplier.apply(this)
        handler.postDelayed({ applying = false }, 350)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!VolumePrefs.isEnabled(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        VolumeApplier.apply(this)
        return START_STICKY
    }

    override fun onDestroy() {
        observer?.let { contentResolver.unregisterContentObserver(it) }
        observer = null
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "음량 고정 상태",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        return builder
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentTitle("Volume Lock 실행 중")
            .setContentText("설정한 음량을 감시하고 있습니다.")
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "volume_lock_channel"
        const val NOTIFICATION_ID = 101
    }
}
