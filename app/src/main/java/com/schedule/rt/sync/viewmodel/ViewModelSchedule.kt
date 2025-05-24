package com.schedule.rt.sync.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassCourse
import java.time.LocalDate

class ViewModelSchedule: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("courses")

    fun getCurrentDay(context: Context): LiveData<String?> {
        val currentDay = MutableLiveData<String?>()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val getDay = LocalDate
                    .now()
                    .dayOfWeek
                    .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("en", "US"))
                    .lowercase()
                currentDay.postValue(getDay)
            }
        }

        // Register the receiver
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_TICK) // Optional: update setiap menit
        }

        context.registerReceiver(receiver, filter)

        // Set nilai awal saat fungsi dipanggil
        val getDay = LocalDate
            .now()
            .dayOfWeek
            .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("en", "US"))
            .lowercase()
        currentDay.postValue(getDay)

        return currentDay
    }

    private val _day = MutableLiveData<String?>()
    val day: LiveData<String?> = _day

    fun sendDay(day: String?) {
        _day.value = day
    }

    private val _uidBuildingReference = MutableLiveData<String?>()
    val uidBuildingReference: LiveData<String?> = _uidBuildingReference

    fun uidBuildingReference(uidBuilding: String?) {
        _uidBuildingReference.value = uidBuilding
    }

    private val _uidRoomReference = MutableLiveData<String?>()
    val uidRoomReference: LiveData<String?> = _uidRoomReference

    fun uidRoomReference(uidRoom: String?) {
        _uidRoomReference.value = uidRoom
    }

    fun getAllSchedule(): LiveData<List<DataClassCourse>> {
        val dataSchedule = MutableLiveData<List<DataClassCourse>>()
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val listSchedule = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                    val getDay = getSchedule?.day
                    val getUidBuilding = getSchedule?.uidBuilding
                    val getUidRoom = getSchedule?.uidRoom
                    val getStartTime = getSchedule?.startTime
                    val getEndTime = getSchedule?.endTime
                    if (getDay != null && getUidBuilding != null && getUidRoom != null && getStartTime != null && getEndTime != null) {
                        getSchedule.let { listSchedule.add(it) }
                    }
                }
                dataSchedule.value = listSchedule.sortedByStartTime()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        return dataSchedule
    }

    fun getSchedule(day: String?): LiveData<List<DataClassCourse>> {
        val dataSchedule = MutableLiveData<List<DataClassCourse>>()
        val ref = databaseReference.orderByChild("uidRoom").equalTo(uidRoomReference.value)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listSchedule = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                    val scheduleDay = getSchedule?.day
                    if (scheduleDay == day) {
                        getSchedule?.let { listSchedule.add(it) }
                    }
                }
                dataSchedule.value = listSchedule.sortedByStartTime()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelSchedule", "Error")
            }
        })
        return dataSchedule
    }

    fun getScheduleForStudent(uidClasses: String?, day: String?): LiveData<List<DataClassCourse>> {
        val dataSchedule = MutableLiveData<List<DataClassCourse>>()
        val ref = databaseReference.orderByChild("uidClasses").equalTo(uidClasses)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listSchedule = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getCourse = dataSnapshot.getValue(DataClassCourse::class.java)
                    if (getCourse?.day == day) {
                        getCourse?.let { listSchedule.add(it) }
                    }
                }
                dataSchedule.value = listSchedule.sortedByStartTime()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelSchedule", "Error")
            }
        })
        return dataSchedule
    }

    fun getScheduleForLecturer(uidLecturer: String?, day: String?): LiveData<List<DataClassCourse>> {
        val dataSchedule = MutableLiveData<List<DataClassCourse>>()
        val ref = databaseReference.orderByChild("uidLecturer").equalTo(uidLecturer)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listSchedule = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                    if (getSchedule?.day == day) {
                        getSchedule?.let { listSchedule.add(it) }
                    }
                }
                dataSchedule.value = listSchedule.sortedByStartTime()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelSchedule", "Error")
            }
        })

        return dataSchedule
    }

    private val _startTime = MutableLiveData<String?>()
    val startTime: LiveData<String?> = _startTime

    private val _endTime = MutableLiveData<String?>()
    val endTime: LiveData<String?> = _endTime

    fun sendStartTime(startTime: String?) {
        _startTime.value = startTime
    }

    fun sendEndTime(endTime: String?) {
        _endTime.value = endTime
    }

    fun addSchedule(uidCourse: String?, uidLecturer: String?, uidClasses: String?): LiveData<String?> {
        val startTimeInput = stringTimeToInt(startTime.value.toString())
        val endTimeInput = stringTimeToInt(endTime.value.toString())
        val result = MutableLiveData<String?>()

        if (endTimeInput > 1439) {
            result.value = "Invalid time"
        } else {
            val ref = databaseReference.orderByChild("uidRoom").equalTo(uidRoomReference.value)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var isScheduleConflict = false
                    for (dataSnapshot in snapshot.children) {
                        val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                        val scheduleDay = getSchedule?.day
                        if (scheduleDay == day.value) {
                            val startTimeRoom = stringTimeToInt(getSchedule?.startTime.toString())
                            val endTimeRoom = stringTimeToInt(getSchedule?.endTime.toString())
                            if (startTimeInput in startTimeRoom..endTimeRoom || endTimeInput in startTimeRoom..endTimeRoom) {
                                isScheduleConflict = true
                                break
                            }
                        }
                    }

                    if (isScheduleConflict == true) {
                        result.value = "ScheduleConflict"
                    } else {
                        val dayRef = databaseReference.orderByChild("day").equalTo(day.value)
                        dayRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val lecturerSchedule = mutableListOf<DataClassCourse>()
                                for (dataSnapshot in snapshot.children) {
                                    val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                                    if (getSchedule?.uidLecturer == uidLecturer) {
                                        getSchedule?.let { lecturerSchedule.add(it) }
                                    }
                                }

                                var isLecturerConflict = false
                                for (schedule in lecturerSchedule) {
                                    val startTimeLecturer = stringTimeToInt(schedule.startTime.toString())
                                    val endTimeLecturer = stringTimeToInt(schedule.endTime.toString())
                                    if (startTimeInput in startTimeLecturer..endTimeLecturer || endTimeInput in startTimeLecturer..endTimeLecturer) {
                                        isLecturerConflict = true
                                        break
                                    }
                                }

                                if (isLecturerConflict == true) {
                                    result.value = "LecturerConflict"
                                } else {
                                    val classRef = databaseReference.orderByChild("day").equalTo(day.value)
                                    classRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            val classSchedule = mutableListOf<DataClassCourse>()
                                            for (snapshot in snapshot.children) {
                                                val getSchedule = snapshot.getValue(DataClassCourse::class.java)
                                                if (getSchedule?.uidClasses == uidClasses) {
                                                    getSchedule?.let { classSchedule.add(it) }
                                                }
                                            }

                                            var isClassConflict = false
                                            for (schedule in classSchedule) {
                                                val startTimeClass = stringTimeToInt(schedule.startTime.toString())
                                                val endTimeClass = stringTimeToInt(schedule.endTime.toString())
                                                if (startTimeInput in startTimeClass..endTimeClass || endTimeInput in startTimeClass..endTimeClass) {
                                                    isClassConflict = true
                                                    break
                                                }
                                            }

                                            if (isClassConflict == true) {
                                                result.value = "ClassConflict"
                                            } else {
                                                val dataSchedule = mapOf(
                                                    "uidBuilding" to uidBuildingReference.value,
                                                    "uidRoom" to uidRoomReference.value,
                                                    "day" to day.value,
                                                    "startTime" to startTime.value,
                                                    "endTime" to endTime.value
                                                )

                                                databaseReference.child(uidCourse.toString()).updateChildren(dataSchedule).addOnCompleteListener {
                                                    result.value = "Success"
                                                }.addOnFailureListener {
                                                    result.value = "Fail"
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.e("ViewModelSchedule", "Error")
                                        }
                                    })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e("ViewModelSchedule", "Error")
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ViewModelSchedule", "Error")
                }
            })
        }
        return result
    }

    fun deleteSchedule(uidCourse: String?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val dataSchedule = mapOf(
            "uidBuilding" to null,
            "uidRoom" to null,
            "day" to null,
            "uidRoomDay" to null,
            "startTime" to null,
            "endTime" to null
        )
        databaseReference.child(uidCourse.toString()).updateChildren(dataSchedule).addOnCompleteListener {
            result.value = "Success"
        }.addOnFailureListener {
            result.value = "Fail"
        }
        return result
    }

    private fun stringTimeToInt(time: String): Int {
        val timeSplit = time.split(":")
        val hour = timeSplit[0].toInt()
        val minute = timeSplit[1].toInt()
        return hour * 60 + minute
    }

    private fun List<DataClassCourse>.sortedByStartTime(): List<DataClassCourse> {
        return this.sortedBy { course ->
            course.startTime?.let {
                val (hour, minute) = it.split(":").map { it.toInt() }
                hour * 60 + minute
            }
        }
    }
}