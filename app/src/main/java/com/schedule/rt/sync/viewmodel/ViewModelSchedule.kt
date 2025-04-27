package com.schedule.rt.sync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassCourse

class ViewModelSchedule: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("courses")

    var uidBuilding: String? = null
    var uidRoom: String? = null
    var day: String? = null

    fun getSchedule(day: String?): LiveData<List<DataClassCourse>> {
        val dataSchedule = MutableLiveData<List<DataClassCourse>>()
        val uidRoomDay = buildString {
            append(uidRoom)
            append("_")
            append(day)
        }
        val ref = databaseReference.orderByChild("uidRoomDay").equalTo(uidRoomDay)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listSchedule = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                    getSchedule?.let { listSchedule.add(it) }
                }
                dataSchedule.value = listSchedule
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return dataSchedule
    }

    var startTime: String? = null
    var endTime: String? = null

    fun addSchedule(uidCourse: String?, uidLecturer: String?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val uidRoomDay = buildString {
            append(uidRoom)
            append("_")
            append(day)

        }
        val ref = databaseReference.orderByChild("uidRoomDay").equalTo(uidRoomDay)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isScheduleConflict = false
                for (dataSnapshot in snapshot.children) {
                    val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                    val startTimeSchedule = getSchedule?.startTime
                    val endTimeSchedule = getSchedule?.endTime
                    val startTimeScheduleInt = stringTimeToInt(startTimeSchedule.toString())
                    val endTimeScheduleInt = stringTimeToInt(endTimeSchedule.toString())
                    val startTimeInt = stringTimeToInt(startTime.toString())
                    val endTimeInt = stringTimeToInt(endTime.toString())
                    if (startTimeInt in startTimeScheduleInt..endTimeScheduleInt || endTimeInt in startTimeScheduleInt..endTimeScheduleInt) {
                        isScheduleConflict = true
                        break
                    }
                }

                if (isScheduleConflict == true) {
                    result.value = "ScheduleConflict"
                } else {
                    val dayRef = databaseReference.orderByChild("day").equalTo(day)
                    dayRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val scheduleWithLecturer = mutableListOf<DataClassCourse>()
                            for (dataSnapshot in snapshot.children) {
                                val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                                if (getSchedule?.uidLecturer == uidLecturer) {
                                    getSchedule?.let { scheduleWithLecturer.add(it) }
                                }
                            }

                            var isLecturerConflict = false
                            for (schedule in scheduleWithLecturer) {
                                val startTimeSchedule = schedule.startTime
                                val endTimeSchedule = schedule.endTime
                                val startTimeScheduleInt = stringTimeToInt(startTimeSchedule.toString())
                                val endTimeScheduleInt = stringTimeToInt(endTimeSchedule.toString())
                                val startTimeInt = stringTimeToInt(startTime.toString())
                                val endTimeInt = stringTimeToInt(endTime.toString())
                                if (startTimeInt in startTimeScheduleInt..endTimeScheduleInt || endTimeInt in startTimeScheduleInt..endTimeScheduleInt) {
                                    isLecturerConflict = true
                                    break
                                }
                            }

                            if (isLecturerConflict == true) {
                                result.value = "LecturerConflict"
                            } else {
                                val dataSchedule = mapOf(
                                    "uidBuilding" to uidBuilding,
                                    "uidRoom" to uidRoom,
                                    "day" to day,
                                    "uidRoomDay" to buildString {
                                        append(uidRoom)
                                        append("_")
                                        append(day)
                                    },
                                    "startTime" to startTime,
                                    "endTime" to endTime
                                )

                                databaseReference.child(uidCourse.toString()).updateChildren(dataSchedule).addOnCompleteListener {
                                    result.value = "Success"
                                }.addOnFailureListener {
                                    result.value = "Fail"
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            TODO("Not yet implemented")
                        }
                    })

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
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
}