package com.schedule.rt.sync.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassCourse
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class ForegroundService : Service() {

    private val channelId = "alarm_channel"
    private val baseNotificationId = 1001
    private val handler = Handler(Looper.getMainLooper())
    private val activeCountdowns = ConcurrentHashMap<String, Runnable>()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var isForegroundNotificationActive = false
    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val uidClasses = intent?.getStringExtra("uidClasses")
        val uidLecturer = intent?.getStringExtra("uidLecturer")
        Log.d("ForegroundService", "Service started with uidClasses: $uidClasses, uidLecturer: $uidLecturer")

        val dayToday = LocalDate.now().dayOfWeek.name.lowercase()
        Log.d("ForegroundService", "Today is: $dayToday")

        if (uidLecturer != null) {
            lecturerScheduleListener(uidLecturer, dayToday)
        } else if (uidClasses != null) {
            studentScheduleListener(uidClasses, dayToday)
        }

        return START_STICKY
    }

    private fun startForegroundService() {
        isForegroundNotificationActive = true
        startForeground(baseNotificationId, createServiceNotification())
    }

    private fun stopForegroundService() {
        if (isForegroundNotificationActive) {
            stopForeground(true)
            isForegroundNotificationActive = false
        }
    }

    private fun studentScheduleListener(uidClasses: String, day: String) {
        val ref = FirebaseDatabase.getInstance().getReference("courses")
        val studentRef = ref.orderByChild("uidClasses").equalTo(uidClasses)
        studentRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val now = System.currentTimeMillis()
                Log.d("ForegroundService", "Processing schedules for uidClasses: $uidClasses, day: $day")

                for (dataSnap in snapshot.children) {
                    val schedule = dataSnap.getValue(DataClassCourse::class.java)
                    if (schedule == null) {
                        Log.w("ForegroundService", "Invalid schedule data: $dataSnap")
                        continue
                    }

                    Log.d("ForegroundService", "Schedule: ${schedule.nameCourse}, startTime: ${schedule.startTime}")
                    if (schedule.day == day && schedule.startTime != null) {
                        processSchedule(schedule, now)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ForegroundService", "Firebase Error: ${error.message}")
            }
        })
    }

    private fun lecturerScheduleListener(uidLecturer: String, day: String) {
        val ref = FirebaseDatabase.getInstance().getReference("courses")
        val lecturerRef = ref.orderByChild("uidLecturer").equalTo(uidLecturer)
        lecturerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val now = System.currentTimeMillis()
                Log.d("ForegroundService", "Processing schedules for uidLecturer: $uidLecturer, day: $day")

                for (dataSnap in snapshot.children) {
                    val schedule = dataSnap.getValue(DataClassCourse::class.java)
                    if (schedule == null) {
                        Log.w("ForegroundService", "Invalid schedule data: $dataSnap")
                        continue
                    }

                    Log.d("ForegroundService", "Schedule: ${schedule.nameCourse}, startTime: ${schedule.startTime}")
                    if (schedule.day == day && schedule.startTime != null) {
                        processSchedule(schedule, now)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ForegroundService", "Firebase Error: ${error.message}")
            }
        })
    }

    private fun processSchedule(schedule: DataClassCourse, now: Long) {
        try {
            val startTime = LocalTime.parse(schedule.startTime, timeFormatter)
            val today = LocalDate.now()
            val startMillis = startTime.atDate(today)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val oneHourBeforeMillis = startMillis - TimeUnit.HOURS.toMillis(1)

            Log.d("ForegroundService", "Schedule: ${schedule.nameCourse}, startMillis: $startMillis, now: $now")

            if (now in oneHourBeforeMillis..startMillis) {
                val timeLeftMillis = startMillis - now
                val scheduleKey = "${schedule.nameCourse}_${schedule.startTime}"
                Log.d("ForegroundService", "Starting countdown for ${schedule.nameCourse}, time left: ${timeLeftMillis / 1000} seconds")
                startCountdownNotification(schedule, timeLeftMillis, scheduleKey)
            } else {
                Log.d("ForegroundService", "Schedule ${schedule.nameCourse} not in 1-hour window")
            }
        } catch (e: Exception) {
            Log.e("ForegroundService", "Error parsing startTime: ${schedule.startTime} for ${schedule.nameCourse}", e)
        }
    }

    private fun startCountdownNotification(course: DataClassCourse, timeLeftMillis: Long, scheduleKey: String) {
        val notificationId = baseNotificationId + scheduleKey.hashCode()

        // Hide foreground notification
        stopForegroundService()

        // Hentikan countdown sebelumnya untuk jadwal ini, jika ada
        activeCountdowns[scheduleKey]?.let { handler.removeCallbacks(it) }

        val totalTimeMillis = TimeUnit.HOURS.toMillis(1) // 3600000 ms (1 hour)
        val startTimeMillis = System.currentTimeMillis()
        val endTimeMillis = startTimeMillis + timeLeftMillis

        val runnable = object : Runnable {
            override fun run() {
                val currentTimeMillis = System.currentTimeMillis()
                val remainingMillis = endTimeMillis - currentTimeMillis

                if (remainingMillis <= 0) {
                    val builder = NotificationCompat.Builder(this@ForegroundService, channelId)
                        .setContentTitle("${course.nameCourse} ${course.sksCourse} SKS")
                        .setContentText("Class is starting now!")
                        .setSmallIcon(R.drawable.schedule)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                    notificationManager.notify(notificationId, builder.build())
                    activeCountdowns.remove(scheduleKey)
                    // Restore foreground notification
                    startForegroundService()
                    return
                }

                val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
                val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60
                // Calculate progress: 100% at start, 0% at end
                val progress = (remainingMillis.toFloat() / totalTimeMillis * 100).toInt()
                Log.d("ForegroundService", "Countdown: ${course.nameCourse}, remaining: ${remainingMillis}ms, progress: $progress%")

                val builder = NotificationCompat.Builder(this@ForegroundService, channelId)
                    .setContentTitle("${course.nameCourse} ${course.sksCourse} SKS")
                    .setContentText("Starts in ${minutesLeft}m ${secondsLeft}s")
                    .setSmallIcon(R.drawable.schedule)
                    .setProgress(100, progress, false)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(false)

                notificationManager.notify(notificationId, builder.build())

                handler.postDelayed(this, 1000)
            }
        }

        activeCountdowns[scheduleKey] = runnable
        handler.post(runnable)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Kuliah",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(true)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Jadwal Kuliah")
            .setContentText("Monitoring upcoming schedules...")
            .setSmallIcon(R.drawable.schedule)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        activeCountdowns.values.forEach { handler.removeCallbacks(it) }
        activeCountdowns.clear()
        Log.d("ForegroundService", "Service destroyed")
    }
}