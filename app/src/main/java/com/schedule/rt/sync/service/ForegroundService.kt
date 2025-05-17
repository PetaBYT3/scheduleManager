package com.schedule.rt.sync.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.dataclass.DataClassUser
import com.schedule.rt.sync.userpreferences.SettingsPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class ForegroundService : Service() {

    private val channelId = "alarm_channel"
    private val baseNotificationId = 1001
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private var isForegroundNotificationActive = false
    private val notificationManager by lazy { getSystemService(NotificationManager::class.java) }
    private val settingsPreferences by lazy { SettingsPreferences(this) }
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val notificationGroup = "countdown_group" // Untuk mengelompokkan notifikasi

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val dayMap = mapOf(
            "kamis" to "Thursday",
            "senin" to "Monday",
            "selasa" to "Tuesday",
            "rabu" to "Wednesday",
            "jumat" to "Friday",
            "sabtu" to "Saturday",
            "minggu" to "Sunday"
        )
        val rawDay = LocalDate.now().dayOfWeek.name.lowercase()
        val dayToday = dayMap[rawDay] ?: rawDay.replaceFirstChar { it.uppercase() }
        Log.d("ForegroundService", "Today is: $dayToday")

        val currentUser = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("ForegroundService", "Current user UID: $currentUser")
        if (currentUser != null) {
            val userDataBaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.toString())
            userDataBaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val getUser = snapshot.getValue(DataClassUser::class.java)
                        Log.d("ForegroundService", "User data retrieved: $getUser")
                        val uidLecturer = getUser?.uidLecturer
                        val uidClasses = getUser?.uidClasses
                        if (uidLecturer != null) {
                            lecturerScheduleListener(uidLecturer, dayToday)
                        } else if (uidClasses != null) {
                            studentScheduleListener(uidClasses, dayToday)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ForegroundService", "Firebase Error: ${error.message}")
                }
            })
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForegroundService()
        createServiceNotification()
    }

    private fun startForegroundService() {
        if (!isForegroundNotificationActive) {
            isForegroundNotificationActive = true
            startForeground(baseNotificationId, createServiceNotification())
        }
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
                    if (schedule == null || schedule.startTime == null || schedule.day == null || schedule.nameCourse == null || schedule.sksCourse == null) {
                        Log.w("ForegroundService", "Invalid schedule data: $dataSnap")
                        continue
                    }

                    Log.d("ForegroundService", "Schedule: ${schedule.nameCourse}, startTime: ${schedule.startTime}")
                    val dayMap = mapOf(
                        "kamis" to "Thursday",
                        "senin" to "Monday",
                        "selasa" to "Tuesday",
                        "rabu" to "Wednesday",
                        "jumat" to "Friday",
                        "sabtu" to "Saturday",
                        "minggu" to "Sunday"
                    )
                    val normalizedDay = dayMap[schedule.day!!.lowercase()] ?: schedule.day
                    if (normalizedDay.equals(day, ignoreCase = true)) {
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
                    if (schedule == null || schedule.startTime == null || schedule.day == null || schedule.nameCourse == null || schedule.sksCourse == null) {
                        Log.w("ForegroundService", "Invalid schedule data: $dataSnap")
                        continue
                    }

                    Log.d("ForegroundService", "Schedule: ${schedule.nameCourse}, startTime: ${schedule.startTime}")
                    val dayMap = mapOf(
                        "kamis" to "Thursday",
                        "senin" to "Monday",
                        "selasa" to "Tuesday",
                        "rabu" to "Wednesday",
                        "jumat" to "Friday",
                        "sabtu" to "Saturday",
                        "minggu" to "Sunday"
                    )
                    val normalizedDay = dayMap[schedule.day!!.lowercase()] ?: schedule.day
                    if (normalizedDay.equals(day, ignoreCase = true)) {
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
        serviceScope.launch {
            settingsPreferences.getAlarmDelayMinutes.collect { alarmDelayMinutes ->
                try {
                    if (schedule.startTime?.matches("\\d{2}:\\d{2}".toRegex()) != true) {
                        Log.w("ForegroundService", "Invalid startTime format: ${schedule.startTime} for ${schedule.nameCourse}")
                        return@collect
                    }
                    val startTime = LocalTime.parse(schedule.startTime, timeFormatter)
                    val today = LocalDate.now()
                    val startMillis = startTime.atDate(today)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    val delayBeforeMillis = startMillis - TimeUnit.MINUTES.toMillis(alarmDelayMinutes.toLong())

                    Log.d("ForegroundService", "Schedule: ${schedule.nameCourse}, startMillis: $startMillis, now: $now, alarmDelay: $alarmDelayMinutes minutes")
                    Log.d("ForegroundService", "Checking window: now=$now, delayBeforeMillis=$delayBeforeMillis, startMillis=$startMillis")

                    if (now in delayBeforeMillis..startMillis) {
                        val timeLeftMillis = startMillis - now
                        val scheduleKey = "${schedule.nameCourse}_${schedule.startTime}"
                        Log.d("ForegroundService", "Starting countdown for ${schedule.nameCourse}, time left: ${timeLeftMillis / 1000} seconds")
                        startCountdownNotification(schedule, timeLeftMillis, scheduleKey, alarmDelayMinutes)
                    } else {
                        Log.d("ForegroundService", "Schedule ${schedule.nameCourse} not in $alarmDelayMinutes-minute window")
                    }
                } catch (e: Exception) {
                    Log.e("ForegroundService", "Error parsing startTime: ${schedule.startTime} for ${schedule.nameCourse}", e)
                }
            }
        }
    }

    private fun startCountdownNotification(course: DataClassCourse, timeLeftMillis: Long, scheduleKey: String, alarmDelayMinutes: Int) {
        val notificationId = baseNotificationId + scheduleKey.hashCode()
        val totalTimeMillis = TimeUnit.MINUTES.toMillis(alarmDelayMinutes.toLong())
        var remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillis)

        serviceScope.launch {
            // Tampilkan notifikasi awal
            val initialMinutesLeft = remainingMinutes
            val initialProgress = (timeLeftMillis.toFloat() / totalTimeMillis * 100).toInt()
            Log.d("ForegroundService", "Initial Countdown: ${course.nameCourse}, minutes left: $initialMinutesLeft, progress: $initialProgress%, ID: $notificationId")

            val initialNotification = NotificationCompat.Builder(this@ForegroundService, channelId)
                .setContentTitle("${course.nameCourse} ${course.sksCourse} SKS")
                .setContentText("Starts in ${initialMinutesLeft}m")
                .setSmallIcon(R.drawable.schedule)
                .setProgress(100, initialProgress, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setGroup(notificationGroup) // Kelompokkan notifikasi
                .build()

            withContext(Dispatchers.Main) {
                notificationManager.notify(notificationId, initialNotification)
            }

            // Mulai countdown per menit
            while (remainingMinutes > 0) {
                delay(60000) // Tunggu 1 menit
                val now = System.currentTimeMillis()
                val remainingMillis = course.startTime?.let {
                    val startTime = LocalTime.parse(it, timeFormatter)
                    val startMillis = startTime.atDate(LocalDate.now())
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                    startMillis - now
                } ?: 0

                if (remainingMillis <= 0) break

                remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
                val progress = (remainingMillis.toFloat() / totalTimeMillis * 100).toInt()

                val countdownNotification = NotificationCompat.Builder(this@ForegroundService, channelId)
                    .setContentTitle("${course.nameCourse} ${course.sksCourse} SKS")
                    .setContentText("Starts in ${remainingMinutes}m")
                    .setSmallIcon(R.drawable.schedule)
                    .setProgress(100, progress, false)
                    .setOngoing(true)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setAutoCancel(false)
                    .setGroup(notificationGroup) // Kelompokkan notifikasi
                    .build()
                START_STICKY

                withContext(Dispatchers.Main) {
                    notificationManager.notify(notificationId, countdownNotification)
                }
            }

            // Countdown selesai
            val finishedNotification = NotificationCompat.Builder(this@ForegroundService, channelId)
                .setContentTitle("${course.nameCourse} ${course.sksCourse} SKS")
                .setContentText("Class is starting now!")
                .setSmallIcon(R.drawable.schedule)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setGroup(notificationGroup) // Kelompokkan notifikasi
                .build()

            withContext(Dispatchers.Main) {
                notificationManager.notify(notificationId, finishedNotification)
            }
        }
    }

    private fun createNotificationChannel() {
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val attributes = AudioAttributes.Builder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Kuliah",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(notificationSound, null)
                enableVibration(true)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        } else {
            Log.d("ForegroundService", "Android version < Oreo, skipping NotificationChannel creation")
        }
    }

    private fun createServiceNotification(): Notification {
        val serviceNotification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Service Active")
            .setContentText("Foreground service is monitoring incoming schedules. Incoming schedules will be notified")
            .setSmallIcon(R.drawable.schedule)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
        return serviceNotification
    }

    override fun onBind(intent: Intent?): IBinder? = null
}