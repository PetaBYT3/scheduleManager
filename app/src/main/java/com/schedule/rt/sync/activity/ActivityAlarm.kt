package com.schedule.rt.sync.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.schedule.rt.sync.R
import com.schedule.rt.sync.databinding.ActivityAlarmBinding

class ActivityAlarm : AppCompatActivity() {

    private lateinit var binding: ActivityAlarmBinding

    private var mediaPlayer: MediaPlayer? = null
    private var isAlarmStarted = false

    private var countDownMills: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        countDownMills = intent.getLongExtra("countDownMills", 0)
        binding.pbCountDown.max = 30 * 60

        startAlarmSound()
        startCountDown()

        binding.btnMinimize.setOnClickListener {
            stopAlarmSound()
            showFinalNotification()
            finish()
        }
    }

    private fun startAlarmSound() {
        try {
            mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_ALARM_ALERT_URI)
            mediaPlayer?.apply {
                isLooping = true
                start()
                isAlarmStarted = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopAlarmSound() {
        if (isAlarmStarted) {
            try {
                mediaPlayer?.apply {
                    if (isPlaying) stop()
                    release()
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } finally {
                mediaPlayer = null
                isAlarmStarted = false
            }
        }
    }

    private fun startCountDown() {
        countdownRunnable = object : Runnable {
            var timeLeft = countDownMills

            override fun run() {
                val totalSeconds = timeLeft / 1000
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                binding.tvCountDown.text = String.format("%02d:%02d", minutes, seconds)
                binding.pbCountDown.progress = (30 * 60 - totalSeconds).toInt()

                timeLeft -= 1000
                if (timeLeft > 0) {
                    handler.postDelayed(this, 1000)
                } else {
                    stopAlarmSound()
                    finish()
                }
            }
        }
        countdownRunnable?.let { handler.post(it) }
    }

    private fun showFinalNotification() {
        val channelId = "alarm_channel"
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Kuliah",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pengingat Kuliah")
            .setContentText("Kelas nameCourse dimulai pukul startTime") // bisa kamu sesuaikan nanti
            .setSmallIcon(R.drawable.schedule)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        manager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAlarmSound()
        countdownRunnable?.let { handler.removeCallbacks(it) }
    }
}
