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

    private val _questionResult = MutableLiveData<String>()
    val questionResult: LiveData<String> get() = _questionResult

    fun clearQuestionResult() {
        _questionResult.value = ""
    }

    private val _processResult = MutableLiveData<String>()
    val processResult: LiveData<String> get() = _processResult

    private fun sendResult(result: String) {
        _processResult.value = result
    }

    fun answerCustomQuestion(question: String, useScheduleContext: Boolean) {
        if (useScheduleContext) {
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

                        // Log data yang akan dikirim ke AI
                        Log.d("GenerativeAiViewModel", "Building Data: $buildingData")
                        Log.d("GenerativeAiViewModel", "Room Data: $roomData")
                        Log.d("GenerativeAiViewModel", "Schedule Data: $scheduleData")

                        // Prompt dengan konteks jadwal
                        val aiPrompt = """
                            You are an assistant for a university scheduling system. Answer the user's question about room availability or scheduling based on the provided data. Use natural, conversational language in Indonesian, and ensure the response is accurate by checking for conflicts or availability in the existing schedules. Use 24-hour time format (HH:MM) and lowercase English days (monday, tuesday, ..., friday).

                            **User Question**:
                            $question

                            **Data**:
                            - **Buildings**:
                              ```json
                              ${buildingData.toString()}
                              ```
                            - **Rooms**:
                              ```json
                              ${roomData.toString()}
                              ```
                            - **Existing Schedules**:
                              ```json
                              ${scheduleData.toString()}
                              ```

                            **Constraints**:
                            - Operating hours: 06:00–17:00, Monday to Friday.
                            - Each SKS (credit unit) equals 45 minutes.
                            - Check for conflicts where a room, class (`uidClasses`), or lecturer (`uidLecturer`) is already scheduled during the requested time and day.
                            - If the question involves room availability (e.g., "Is room G205 free on Tuesday from 13:00 to 17:00?"), verify if the room is unoccupied during the specified time by checking existing schedules.
                            - If the room name in the question (e.g., G205) does not match any `name` in the rooms data, assume it refers to a room with a similar name or return a message indicating the room is not found.
                            - Provide a clear, concise answer in Indonesian, avoiding technical jargon unless necessary.

                            **Output**:
                            A natural language response in Indonesian, answering the user's question based on the data. Do not return JSON or code unless explicitly requested.
                        """.trimIndent()

                        Log.d("GenerativeAiViewModel", "Custom Question Prompt (Schedule Context ON): $aiPrompt")
                        getResponseFromAi(aiPrompt)

                        // Hapus observer
                    }
                }
            }
        } else {
            // Prompt tanpa konteks jadwal
            val aiPrompt = """
                You are a helpful assistant. Answer the user's question in natural, conversational Indonesian. Provide a clear, concise response, avoiding technical jargon unless necessary. Do not use any external data or context unless explicitly provided in the question.

                **User Question**:
                $question

                **Output**:
                A natural language response in Indonesian. Do not return JSON or code unless explicitly requested.
            """.trimIndent()

            Log.d("GenerativeAiViewModel", "Custom Question Prompt (Schedule Context OFF): $aiPrompt")
            getResponseFromAi(aiPrompt)
        }
    }

    private fun getResponseFromAi(prompt: String){
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
                            _questionResult.value = content
                            sendResult("Success")
                        } else {
//                            _scheduleResult.value = Result.Error("Tidak ada jadwal yang tersedia atau respons AI bukan JSON valid")
                        }
                    } else {
//                        _scheduleResult.value = Result.Error("Error: Response body is null")
                    }
                } else {
                    val responseBody = response.body?.string()
//                    _scheduleResult.value = Result.Error("Error: ${response.code} - ${responseBody ?: "No response body"}")
                }
            } catch (e: IOException) {
//                _scheduleResult.value = Result.Error("Gagal terkoneksi: ${e.message ?: "Unknown network error"}")
            } catch (e: Exception) {
//                _scheduleResult.value = Result.Error("Error: ${e.message ?: "Unknown error"}")
            }
        }
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