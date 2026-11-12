package com.example.volumelock

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private lateinit var masterSwitch: Switch
    private lateinit var statusText: TextView
    private lateinit var rowsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        buildScreen()
        refreshStatus()
        if (VolumePrefs.isEnabled(this)) startMonitor()
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(20))
            setBackgroundColor(0xFFF6F7F9.toInt())
        }

        val title = TextView(this).apply {
            text = "Volume Lock"
            textSize = 27f
            setTextColor(0xFF172033.toInt())
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "원하는 음량만 선택해 지정한 수준으로 유지합니다."
            textSize = 14f
            setTextColor(0xFF657084.toInt())
            setPadding(0, dp(6), 0, dp(18))
        }
        root.addView(subtitle)

        val masterCard = card()
        masterSwitch = Switch(this).apply {
            text = "전체 음량 고정"
            textSize = 17f
            setTextColor(0xFF172033.toInt())
            setPadding(dp(14), dp(14), dp(14), dp(14))
        }
        masterCard.addView(masterSwitch)
        statusText = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFF657084.toInt())
            setPadding(dp(14), 0, dp(14), dp(14))
        }
        masterCard.addView(statusText)
        root.addView(masterCard)

        val sectionTitle = TextView(this).apply {
            text = "개별 음량 설정"
            textSize = 18f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(0xFF172033.toInt())
            setPadding(0, dp(22), 0, dp(10))
        }
        root.addView(sectionTitle)

        rowsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        root.addView(rowsContainer)

        val note = TextView(this).apply {
            text = "참고: 기기 제조사와 Android 버전에 따라 일부 음량 항목이나 백그라운드 자동 실행이 제한될 수 있습니다. 앱을 강제 종료하면 감시가 중단될 수 있습니다."
            textSize = 12f
            setTextColor(0xFF697386.toInt())
            setPadding(dp(2), dp(16), dp(2), 0)
        }
        root.addView(note)

        setContentView(root)

        masterSwitch.isChecked = VolumePrefs.isEnabled(this)
        masterSwitch.setOnCheckedChangeListener { _, checked ->
            VolumePrefs.setEnabled(this, checked)
            if (checked) {
                startMonitor()
                VolumeApplier.apply(this)
            } else {
                stopService(Intent(this, VolumeMonitorService::class.java))
            }
            refreshStatus()
        }

        VolumePrefs.streams.forEach { setting ->
            addStreamRow(setting)
        }
    }

    private fun addStreamRow(setting: VolumePrefs.StreamSetting) {
        val card = card()
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val label = TextView(this).apply {
            text = setting.title
            textSize = 16f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setTextColor(0xFF172033.toInt())
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f)
            gravity = Gravity.CENTER_VERTICAL
        }
        val toggle = Switch(this).apply {
            isChecked = VolumePrefs.isStreamEnabled(this@MainActivity, setting.key)
            contentDescription = "${setting.title} 고정"
        }
        top.addView(label)
        top.addView(toggle)
        card.addView(top)

        val valueText = TextView(this).apply {
            textSize = 13f
            setTextColor(0xFF526078.toInt())
        }
        val seek = SeekBar(this).apply {
            max = 100
            progress = VolumePrefs.getPercent(this@MainActivity, setting)
        }
        fun updateValue() {
            valueText.text = "고정 음량  ${seek.progress}%"
            seek.isEnabled = toggle.isChecked
        }
        updateValue()

        toggle.setOnCheckedChangeListener { _, checked ->
            VolumePrefs.setStreamEnabled(this, setting.key, checked)
            updateValue()
            if (VolumePrefs.isEnabled(this)) VolumeApplier.apply(this)
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                valueText.text = "고정 음량  $progress%"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {
                VolumePrefs.setPercent(this@MainActivity, setting.key, seek.progress)
                if (VolumePrefs.isEnabled(this@MainActivity) &&
                    VolumePrefs.isStreamEnabled(this@MainActivity, setting.key)) {
                    VolumeApplier.apply(this@MainActivity)
                }
            }
        })

        card.addView(valueText)
        card.addView(seek)
        rowsContainer.addView(card)
    }

    private fun card() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(8), dp(14), dp(12))
        setBackgroundColor(0xFFFFFFFF.toInt())
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = dp(10)
        layoutParams = params
        elevation = dp(1).toFloat()
    }

    private fun startMonitor() {
        val intent = Intent(this, VolumeMonitorService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent)
            else startService(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "백그라운드 실행이 제한되었습니다. 기기 설정을 확인해 주세요.", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshStatus() {
        if (!::statusText.isInitialized) return
        statusText.text = if (VolumePrefs.isEnabled(this)) {
            "상태: 고정 기능 켜짐"
        } else {
            "상태: 고정 기능 꺼짐"
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
