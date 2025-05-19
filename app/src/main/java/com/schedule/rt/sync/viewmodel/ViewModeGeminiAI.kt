package com.schedule.rt.sync.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassBuilding
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.dataclass.DataClassRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class ViewModeGeminiAI() : ViewModel() {

    private val client = OkHttpClient()

    sealed class Result {
        object Loading : Result()
        data class Success(val schedule: DataClassCourse) : Result()
        data class Error(val message: String) : Result()
    }

    private val _findScheduleResult = MutableLiveData<DataClassCourse>()
    val findScheduleResult: LiveData<DataClassCourse> get() = _findScheduleResult

    fun clearPreviousResult() {
        _findScheduleResult.value = DataClassCourse(
            nameCourse = null,
            sksCourse = null,
            uidLecturer = null,
            uidMajor = null,
            uidLevel = null,
            uidClasses = null,
            uidCourse = null,
            uidBuilding = null,
            uidRoom = null,
            day = null,
            startTime = null,
            endTime = null
        )
    }

    private val _processResult = MutableLiveData<String>()
    val processResult: LiveData<String> get() = _processResult

    private val _scheduleResult = MutableLiveData<Result>()
    val scheduleResult: LiveData<Result> get() = _scheduleResult

    fun findScheduleTime(dataClassCourse: DataClassCourse) {
        _scheduleResult.value = Result.Loading

        // Validasi input course
        if (dataClassCourse.nameCourse.isNullOrEmpty() || dataClassCourse.sksCourse.isNullOrEmpty() ||
            dataClassCourse.uidClasses.isNullOrEmpty() || dataClassCourse.uidLecturer.isNullOrEmpty()) {
            _scheduleResult.value = Result.Error("Data matakuliah tidak lengkap")
            return
        }

        // Ambil data dari Firebase
        val buildingData = mutableListOf<DataClassBuilding>()
        val roomData = mutableListOf<DataClassRoom>()
        val scheduleData = mutableListOf<DataClassCourse>()

        getAllBuilding().observeForever { buildings ->
            buildingData.clear()
            buildingData.addAll(buildings ?: emptyList())
            getAllRoom().observeForever { rooms ->
                roomData.clear()
                roomData.addAll(rooms ?: emptyList())
                getAllSchedule().observeForever { schedules ->
                    scheduleData.clear()
                    scheduleData.addAll(schedules ?: emptyList())

                    // Validasi roomData
                    if (roomData.isEmpty()) {
                        _scheduleResult.value = Result.Error("Tidak ada data ruangan tersedia untuk penjadwalan")
                        return@observeForever
                    }

                    val sks = dataClassCourse.sksCourse?.toIntOrNull() ?: 0
                    val durationMinutes = sks * 45

                    // Buat prompt
                    val aiPrompt = """
                        Generate a schedule for an unscheduled course based on the provided data. Ensure no conflicts with existing schedules for the same class (`uidClasses`) or lecturer (`uidLecturer`). Each SKS equals 45 minutes. Use 24-hour time format (HH:MM) and lowercase English days (monday, tuesday, ..., friday). Select `uidBuilding` and `uidRoom` from available buildings and rooms, ensuring `uidRoom` belongs to the chosen `uidBuilding`.
                        
                        **Input Data**:
                        - **Unscheduled Course**:
                          ```json
                          {
                            "nameCourse": "${dataClassCourse.nameCourse}",
                            "sksCourse": "${dataClassCourse.sksCourse}",
                            "uidMajor": "${dataClassCourse.uidMajor ?: ""}",
                            "uidLevel": "${dataClassCourse.uidLevel ?: ""}",
                            "uidClasses": "${dataClassCourse.uidClasses}",
                            "uidLecturer": "${dataClassCourse.uidLecturer}",
                            "uidCourse": "${dataClassCourse.uidCourse ?: ""}"
                          }
                          ```
                        - **Buildings**:
                          ```json
                          ${buildingData.toString()}
                          ```
                        - **Rooms** (each room has a `uidBuilding` linking to a building):
                          ```json
                          ${roomData.toString()}
                          ```
                        - **Existing Schedules** (reference for conflicts):
                          ```json
                          ${scheduleData.toString()}
                          ```

                        **Constraints**:
                        - Schedule within operating hours: 06:00–17:00, Friday to Saturday.
                        - Avoid scheduling conflicts where `uidClasses` or `uidLecturer` overlap in time and day.
                        - Duration: ${sks} SKS = ${durationMinutes} minutes (each SKS equals 45 minutes).
                        - All fields must be strings, including `sksCourse`.
                        - If no slot is available or data is insufficient (e.g., no rooms), return an empty object `{}`.
                        - To ensure maximum variety, follow these steps:
                          1. List all valid slots (combinations of day, startTime, and room) that satisfy no conflicts.
                          2. Randomly select one slot from this list, ensuring an equal chance for each valid slot.
                          3. Prefer slots on days, times, or rooms that are less frequently used in existing schedules (e.g., if 'monday' or '08:00' appears multiple times, prioritize other days or times unless no other options exist).
                          4. Distribute schedules evenly across days (monday to friday), start times (06:00, 07:00, ..., 17:00), and available rooms.
                        - Example valid slots (for 2 SKS):
                          - monday, 09:00–10:30, room T1
                          - tuesday, 11:00–12:30, room T2
                          - friday, 13:00–14:30, room T3
                        - Output **must be a JSON object** and **nothing else** (no code, no explanations, no other formats).

                        **Output Format**:
                        Return a single JSON object representing the new schedule:
                        ```json
                        {
                          "nameCourse": "${dataClassCourse.nameCourse}",
                          "sksCourse": "${dataClassCourse.sksCourse}",
                          "uidMajor": "${dataClassCourse.uidMajor ?: ""}",
                          "uidLevel": "${dataClassCourse.uidLevel ?: ""}",
                          "uidClasses": "${dataClassCourse.uidClasses}",
                          "uidLecturer": "${dataClassCourse.uidLecturer}",
                          "uidCourse": "${dataClassCourse.uidCourse ?: ""}",
                          "uidBuilding": "selected_uid",
                          "uidRoom": "selected_uid",
                          "day": "lowercase_day",
                          "startTime": "HH:MM",
                          "endTime": "HH:MM"
                        }
                        ```
                        or an empty object `{}` if no schedule can be created.
                    """.trimIndent()

                    getResponseFromAi(aiPrompt)
                }
            }
        }
    }

    private fun getResponseFromAi(
        prompt: String,
    ) {
        viewModelScope.launch {
            try {
                val payload = JSONObject().apply {
                    put("contents", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        })
                    })
                }

                val apiKey = "AIzaSyDXE526C52gKhGkL_qHY5254CGGVD6S0hA"
                val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
                Log.d("ViewModeGeminiAI", "API Key: $apiKey")

                val requestBody = payload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$apiUrl?key=$apiKey")
                    .post(requestBody)
                    .build()

                val response = withContext(Dispatchers.IO) {
                    client.newCall(request).execute()
                }

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val result = JSONObject(responseBody)
                        val content = result.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        Log.d("ViewModeGeminiAI", "Raw AI response: $content")

                        if (content != null) {
                            parseScheduleResponse(content)
                        } else {
                            _scheduleResult.value = Result.Error("Tidak ada jadwal yang tersedia atau respons AI bukan JSON valid")
                        }
                    } else {
                        _scheduleResult.value = Result.Error("Error: Response body is null")
                    }
                } else {
                    val responseBody = response.body?.string()
                    _scheduleResult.value = Result.Error("Error: ${response.code} - ${responseBody ?: "No response body"}")
                }
            } catch (e: IOException) {
                _scheduleResult.value = Result.Error("Gagal terkoneksi: ${e.message ?: "Unknown network error"}")
            } catch (e: Exception) {
                _scheduleResult.value = Result.Error("Error: ${e.message ?: "Unknown error"}")
            }
        }
    }

    private fun parseScheduleResponse(response: String) {
        try {
            // Tangani kasus tanpa backtick
            var jsonString = response.trim()
            if (jsonString.startsWith("```json") && jsonString.endsWith("```")) {
                jsonString = jsonString.substringAfter("```json").substringBeforeLast("```").trim()
            } else if (jsonString.startsWith("```") && jsonString.endsWith("```")) {
                jsonString = jsonString.substringAfter("```").substringBeforeLast("```").trim()
            }

            // Deteksi kode non-JSON (misalnya, Python)
            if (jsonString.contains("def ") || jsonString.contains("import ") || jsonString.startsWith("python")) {
                Log.e("ViewModeGeminiAI", "Invalid response: Received code instead of JSON")
                return
            }

            Log.d("ViewModeGeminiAI", "JSON to parse: $jsonString")

            val jsonObject = JSONObject(jsonString)
            if (jsonObject.length() == 0) {
                Log.d("ViewModeGeminiAI", "No schedule available (empty JSON)")
                return
            }

            _findScheduleResult.value = DataClassCourse(
                nameCourse = jsonObject.optString("nameCourse", null),
                sksCourse = jsonObject.optString("sksCourse", null),
                uidLecturer = jsonObject.optString("uidLecturer", null),
                uidMajor = jsonObject.optString("uidMajor", null),
                uidLevel = jsonObject.optString("uidLevel", null),
                uidClasses = jsonObject.optString("uidClasses", null),
                uidCourse = jsonObject.optString("uidCourse", null),
                uidBuilding = jsonObject.optString("uidBuilding", null),
                uidRoom = jsonObject.optString("uidRoom", null),
                day = jsonObject.optString("day", null),
                startTime = jsonObject.optString("startTime", null),
                endTime = jsonObject.optString("endTime", null)
            ).also {
                Log.d("ViewModeGeminiAI", "Parsed schedule: $it")
            }
        } catch (e: Exception) {
            Log.e("ViewModeGeminiAI", "Parsing error: ${e.message}", e)
            return
        }
    }

    fun addSchedule(dataCourse: DataClassCourse): LiveData<String?> {
        val uidCourse = dataCourse.uidCourse
        val uidLecturer = dataCourse.uidLecturer
        val uidClasses = dataCourse.uidClasses

        val day = dataCourse.day
        val uidRoom = dataCourse.uidRoom
        val uidBuilding = dataCourse.uidBuilding
        val startTime = dataCourse.startTime
        val endTime = dataCourse.endTime

        val databaseReference = FirebaseDatabase.getInstance().getReference("courses")

        val startTimeInput = stringTimeToInt(startTime.toString())
        val endTimeInput = stringTimeToInt(endTime.toString())
        val result = MutableLiveData<String?>()

        if (endTimeInput > 1439) {
            result.value = "Invalid time"
        } else {
            val ref = databaseReference.orderByChild("uidRoom").equalTo(uidRoom)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var isScheduleConflict = false
                    for (dataSnapshot in snapshot.children) {
                        val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                        val scheduleDay = getSchedule?.day
                        if (scheduleDay == day) {
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
                        val dayRef = databaseReference.orderByChild("day").equalTo(day)
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
                                    val classRef = databaseReference.orderByChild("day").equalTo(day)
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
                                                    "uidBuilding" to uidBuilding,
                                                    "uidRoom" to uidRoom,
                                                    "day" to day,
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
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
        return result
    }

    private fun getAllBuilding(): LiveData<List<DataClassBuilding>> {
        val dataBuilding = MutableLiveData<List<DataClassBuilding>>()
        val ref = FirebaseDatabase.getInstance().getReference("buildings")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listBuilding = mutableListOf<DataClassBuilding>()
                for (dataSnapshot in snapshot.children) {
                    val getBuilding = dataSnapshot.getValue(DataClassBuilding::class.java)
                    getBuilding?.let { listBuilding.add(it) }
                }
                dataBuilding.value = listBuilding
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeGeminiAI", "Failed to load buildings: ${error.message}")
                _scheduleResult.value = Result.Error("Gagal memuat data gedung: ${error.message}")
            }
        })
        return dataBuilding
    }

    private fun getAllRoom(): LiveData<List<DataClassRoom>> {
        val dataRoom = MutableLiveData<List<DataClassRoom>>()
        val ref = FirebaseDatabase.getInstance().getReference("rooms")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listRoom = mutableListOf<DataClassRoom>()
                for (dataSnapshot in snapshot.children) {
                    val getRoom = dataSnapshot.getValue(DataClassRoom::class.java)
                    getRoom?.let { listRoom.add(it) }
                }
                dataRoom.value = listRoom
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeGeminiAI", "Failed to load rooms: ${error.message}")
                _scheduleResult.value = Result.Error("Gagal memuat data ruangan: ${error.message}")
            }
        })
        return dataRoom
    }

    private fun getAllSchedule(): LiveData<List<DataClassCourse>> {
        val dataSchedule = MutableLiveData<List<DataClassCourse>>()
        val ref = FirebaseDatabase.getInstance().getReference("courses")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listSchedule = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                    val day = getSchedule?.day?.lowercase()
                    val uidBuilding = getSchedule?.uidBuilding
                    val uidRoom = getSchedule?.uidRoom
                    val startTime = getSchedule?.startTime
                    val endTime = getSchedule?.endTime
                    if (day != null && uidBuilding != null && uidRoom != null && startTime != null && endTime != null) {
                        getSchedule.let { listSchedule.add(it) }
                    }
                }
                dataSchedule.value = listSchedule
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeGeminiAI", "Failed to load schedules: ${error.message}")
                _scheduleResult.value = Result.Error("Gagal memuat data jadwal: ${error.message}")
            }
        })
        return dataSchedule
    }

    private fun stringTimeToInt(time: String): Int {
        val timeSplit = time.split(":")
        val hour = timeSplit[0].toInt()
        val minute = timeSplit[1].toInt()
        return hour * 60 + minute
    }
}