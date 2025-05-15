package com.schedule.rt.sync.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.schedule.rt.sync.R
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.function.dpToPx
import com.schedule.rt.sync.userpreferences.SettingsPreferences
import com.schedule.rt.sync.viewmodel.ViewModelBuilding
import com.schedule.rt.sync.viewmodel.ViewModelClasses
import com.schedule.rt.sync.viewmodel.ViewModelCourse
import com.schedule.rt.sync.viewmodel.ViewModelLecturer
import com.schedule.rt.sync.viewmodel.ViewModelLevel
import com.schedule.rt.sync.viewmodel.ViewModelMajor
import com.schedule.rt.sync.viewmodel.ViewModelRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class AdapterCourse(
    private val vmLecturer: ViewModelLecturer,
    private val vmMajor: ViewModelMajor,
    private val vmLevel: ViewModelLevel,
    private val vmClasses: ViewModelClasses,
    private val vmCourse: ViewModelCourse,
    private val vmBuilding: ViewModelBuilding,
    private val vmRoom: ViewModelRoom,
    private val lifecycleOwner: LifecycleOwner,
    private val marginToTopItem: Boolean? = null,
    private val tvData1: Boolean? = null,
    private val tvData2: Boolean? = null,
    private val tvData3: Boolean? = null,
    private val tvData4: Boolean? = null,
    private val tvData5: Boolean? = null,
    private val deleteSchedule: Boolean? = null,
    private val deleteScheduleByClasses: Boolean? = null,
    private val addSchedule: Boolean? = null,
    private val btnFirst: Boolean? = null,
    private val btnSecond: Boolean? = null,
    private val onFirstClick: ((DataClassCourse) -> Unit)? = null,
    private val onSecondClick: ((DataClassCourse) -> Unit)? = null,
    private val onNextClick: ((DataClassCourse) -> Unit)? = null,
    private val settingsPreferences: SettingsPreferences? = null,
    private val onCountDown: Boolean? = null
) : RecyclerView.Adapter<AdapterCourse.ViewHolder>() {

    val dataClassCourse = mutableListOf<DataClassCourse>()
    private val handler = Handler(Looper.getMainLooper())
    private val activeCountdowns = ConcurrentHashMap<String, Runnable>()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_parent, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.ivItem.setImageResource(R.drawable.course)
        holder.btnNext.visibility = View.GONE
        holder.pbCountDown.visibility = if (onCountDown == true) View.VISIBLE else View.GONE
        holder.layoutCountDown.visibility = if (onCountDown == true) View.VISIBLE else View.GONE

        if (marginToTopItem == true) {
            val layoutParams = holder.itemView.layoutParams as MarginLayoutParams
            layoutParams.topMargin = if (position == 0) dpToPx(10) else 0
            holder.itemView.layoutParams = layoutParams
        }

        holder.tvData1.visibility = if (tvData1 == true) View.VISIBLE else View.GONE
        holder.tvData2.visibility = if (tvData2 == true) View.VISIBLE else View.GONE
        holder.tvData3.visibility = if (tvData3 == true) View.VISIBLE else View.GONE
        holder.tvData4.visibility = if (tvData4 == true) View.VISIBLE else View.GONE
        holder.tvData5.visibility = if (tvData5 == true) View.VISIBLE else View.GONE

        val currentItem = dataClassCourse[position]
        holder.tvTittle.text = currentItem.nameCourse
        holder.tvData1.text = buildString {
            append(currentItem.sksCourse)
            append(" SKS")
        }

        val uidMajor = currentItem.uidMajor
        val uidLevel = currentItem.uidLevel
        val uidClasses = currentItem.uidClasses

        val majorFlow = vmMajor.getMajorByUid(uidMajor).asFlow()
        val levelFlow = vmLevel.getLevelByUid(uidLevel).asFlow()
        val classesFlow = vmClasses.getClassesByUid(uidClasses).asFlow()

        var nameMajor: String? = null
        var nameLevel: String? = null
        var nameClasses: String? = null

        lifecycleOwner.lifecycleScope.launch {
            combine(majorFlow, levelFlow, classesFlow) { major, level, classes ->
                nameMajor = major?.nameMajor
                nameLevel = level?.level
                nameClasses = classes?.nameClasses
            }.collect {
                holder.tvData2.text = buildString {
                    append("$nameMajor, ")
                    append("Level $nameLevel, ")
                    append("Class $nameClasses")
                }
            }
        }

        vmLecturer.getLecturerByUid(currentItem.uidLecturer).observe(lifecycleOwner) {
            holder.tvData3.text = it?.nameLecturer
        }

        val flowBuilding = vmBuilding.getBuildingByUid(currentItem.uidBuilding).asFlow()
        val flowRoom = vmRoom.getRoomByUid(currentItem.uidRoom).asFlow()

        var nameBuilding: String? = null
        var nameRoom: String? = null

        lifecycleOwner.lifecycleScope.launch {
            combine(flowBuilding, flowRoom) { building, room ->
                nameBuilding = building?.nameBuilding
                nameRoom = room?.nameRoom
            }.collect {
                if (nameBuilding != null && nameRoom != null) {
                    holder.tvData4.text = buildString {
                        append("Building $nameBuilding, ")
                        append("Room $nameRoom")
                    }
                } else {
                    holder.tvData4.text = buildString {
                        append("-")
                    }
                }
            }
        }

        if (currentItem.startTime != null && currentItem.endTime != null) {
            holder.tvData5.text = buildString {
                append(currentItem.startTime)
                append(" - ")
                append(currentItem.endTime)
            }
        } else {
            holder.tvData5.text = buildString {
                append("-")
            }
        }

        if (deleteSchedule == true) {
            val day = currentItem.day
            val building = currentItem.uidBuilding
            val room = currentItem.uidRoom
            val roomDay = currentItem.uidRoomDay
            val startTime = currentItem.startTime
            val endTime = currentItem.endTime

            if (day != null && building != null && room != null && roomDay != null && startTime != null && endTime != null) {
                holder.btnNext.visibility = View.VISIBLE
                holder.ivNext.setImageResource(R.drawable.delete_schedule)
            } else {
                holder.btnNext.visibility = View.GONE
            }
        }

        if (deleteScheduleByClasses == true) {
            val uidClasses = vmCourse.uidClassesReference.value

            if (uidClasses == currentItem.uidClasses) {
                holder.btnNext.visibility = View.VISIBLE
                holder.ivNext.setImageResource(R.drawable.delete_schedule)
            } else {
                holder.btnNext.visibility = View.GONE
            }
        }

        if (addSchedule == true) {
            holder.btnNext.visibility = View.VISIBLE
            holder.ivNext.setImageResource(R.drawable.add_schedule)
        }

        holder.btnFirst.visibility = if (btnFirst == true) View.VISIBLE else View.GONE
        holder.btnSecond.visibility = if (btnSecond == true) View.VISIBLE else View.GONE

        holder.btnFirst.setOnClickListener {
            onFirstClick?.invoke(currentItem)
        }

        holder.btnSecond.setOnClickListener {
            onSecondClick?.invoke(currentItem)
        }

        holder.btnNext.setOnClickListener {
            onNextClick?.invoke(currentItem)
        }

        if (onCountDown == true) {
            startCountdown(holder, currentItem)
        }
    }

    private fun startCountdown(holder: ViewHolder, course: DataClassCourse) {
        // Validasi data
        if (course.startTime == null || course.day == null || settingsPreferences == null) {
            holder.pbCountDown.visibility = View.VISIBLE
            holder.layoutCountDown.visibility = View.VISIBLE
            holder.pbCountDown.progress = 100
            holder.tvCountDown.text = "60:00"
            return
        }

        val scheduleKey = "${course.nameCourse}_${course.startTime}"
        activeCountdowns[scheduleKey]?.let { handler.removeCallbacks(it) }
        activeCountdowns.remove(scheduleKey) // Bersihkan sebelum memulai baru

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsPreferences.getAlarmDelayMinutes.collect { alarmDelayMinutes ->
                    try {
                        // Parse waktu mulai
                        val startTime = LocalTime.parse(course.startTime, timeFormatter)
                        val today = LocalDate.now()
                        val startMillis = startTime.atDate(today)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        // Gunakan ALARM_DELAY untuk jendela countdown
                        val delayBeforeMillis = startMillis - TimeUnit.MINUTES.toMillis(alarmDelayMinutes.toLong())
                        val now = System.currentTimeMillis()

                        holder.pbCountDown.visibility = View.VISIBLE
                        holder.layoutCountDown.visibility = View.VISIBLE

                        // Cek apakah mata kuliah sudah mulai
                        if (now > startMillis) {
                            holder.pbCountDown.progress = 0
                            holder.tvCountDown.text = "00:00"
                            return@collect
                        }

                        // Cek apakah sebelum jendela countdown
                        if (now < delayBeforeMillis) {
                            holder.pbCountDown.progress = 100
                            holder.tvCountDown.text = String.format("%02d:00", alarmDelayMinutes)
                            return@collect
                        }

                        // Dalam jendela countdown
                        var remainingMillis = startMillis - now
                        val totalTimeMillis = TimeUnit.MINUTES.toMillis(alarmDelayMinutes.toLong())

                        // Mulai countdown dengan coroutine
                        while (remainingMillis > 0) {
                            val minutesLeft = TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
                            val secondsLeft = TimeUnit.MILLISECONDS.toSeconds(remainingMillis) % 60
                            withContext(Dispatchers.Main) {
                                holder.tvCountDown.text = String.format("%02d:%02d", minutesLeft, secondsLeft)
                                val progress = (remainingMillis.toFloat() / totalTimeMillis * 100).toInt()
                                holder.pbCountDown.progress = progress
                            }
                            delay(1000)
                            remainingMillis -= 1000
                        }

                        // Countdown selesai
                        withContext(Dispatchers.Main) {
                            holder.pbCountDown.progress = 0
                            holder.tvCountDown.text = "00:00"
                            activeCountdowns.remove(scheduleKey)
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            holder.pbCountDown.visibility = View.VISIBLE
                            holder.layoutCountDown.visibility = View.VISIBLE
                            holder.pbCountDown.progress = 100
                            holder.tvCountDown.text = String.format("%02d:00", alarmDelayMinutes)
                            activeCountdowns.remove(scheduleKey)
                        }
                    }
                }
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        // Hentikan countdown saat view di-recycle
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val course = dataClassCourse[position]
            val scheduleKey = "${course.nameCourse}_${course.startTime}"
            activeCountdowns[scheduleKey]?.let { handler.removeCallbacks(it) }
            activeCountdowns.remove(scheduleKey)
            holder.pbCountDown.visibility = View.VISIBLE
            holder.layoutCountDown.visibility = View.VISIBLE
            holder.pbCountDown.progress = 100
            // Gunakan ALARM_DELAY untuk UI default
            val alarmDelay = try {
                settingsPreferences?.getAlarmDelayMinutes ?: 60
            } catch (e: Exception) {
                60
            }
            holder.tvCountDown.text = String.format("%02d:00", alarmDelay)
        }
    }

    override fun getItemCount(): Int {
        return dataClassCourse.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivItem: ImageView = itemView.findViewById(R.id.ivItem)
        val tvTittle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvData1: TextView = itemView.findViewById(R.id.tvData1)
        val tvData2: TextView = itemView.findViewById(R.id.tvData2)
        val tvData3: TextView = itemView.findViewById(R.id.tvData3)
        val tvData4: TextView = itemView.findViewById(R.id.tvData4)
        val tvData5: TextView = itemView.findViewById(R.id.tvData5)
        val btnFirst: ConstraintLayout = itemView.findViewById(R.id.btnFirst)
        val btnSecond: ConstraintLayout = itemView.findViewById(R.id.btnSecond)
        val ivNext: ImageView = itemView.findViewById(R.id.ivNext)
        val btnNext: ConstraintLayout = itemView.findViewById(R.id.btnNext)
        val pbCountDown: ProgressBar = itemView.findViewById(R.id.pbCountDown)
        val layoutCountDown: LinearLayout = itemView.findViewById(R.id.layoutCountDown)
        val tvCountDown: TextView = itemView.findViewById(R.id.tvCountDown)
    }

    fun updateData(newData: List<DataClassCourse>) {
        dataClassCourse.clear()
        dataClassCourse.addAll(newData)
        notifyDataSetChanged()
    }
}