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
import com.schedule.rt.sync.dataclass.DataClassLecturer
import com.schedule.rt.sync.dataclass.DataClassLevel
import com.schedule.rt.sync.dataclass.DataClassMajor
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

    init {
        getAllBuilding()
        getAllRoom()
        getAllCourse()
        getAllLecturer()
        getAllMajor()
        getAllLevel()
        getAllClasses()
    }

    private val dataBuilding = MutableLiveData<List<DataClassBuilding>>()
    private val dataRoom = MutableLiveData<List<DataClassRoom>>()
    private val dataCourse = MutableLiveData<List<DataClassCourse>>()
    private val dataLecturer = MutableLiveData<List<DataClassLecturer>>()
    private val dataMajor = MutableLiveData<List<DataClassMajor>>()
    private val dataLevel = MutableLiveData<List<DataClassLevel>>()
    private val dataClasses = MutableLiveData<List<DataClassCourse>>()

    fun answerCustomQuestion(question: String, useScheduleContext: Boolean) {
        if (useScheduleContext) {
            val aiPrompt = """
                You are an assistant for a university scheduling system. Answer the user's question about room availability, lecturer availability, course scheduling, or related academic information based on the provided data. Use natural, conversational language in Indonesian, and ensure the response is accurate by checking for conflicts or availability in the existing schedules. Use 24-hour time format (HH:MM) and lowercase English days (monday, tuesday, ..., saturday).

                **User Question**:
                $question

                **Data**:
                - **Buildings**:
                  ```json
                  ${dataBuilding.value}
                  ```
                - **Rooms**:
                  ```json
                  ${dataRoom.value}
                  ```
                - **Courses (Mata Kuliah)**:
                  ```json
                  ${dataCourse.value}
                  ```
                  Note: If `day`, `uidBuilding`, `uidRoom`, `startTime`, and `endTime` are `null` in a Course entry, it means the course has not been scheduled yet. Courses may also reference `uidMajor`, `uidLevel`, and `uidClasses` to link to specific programs, levels, and classes.
                - **Lecturers**:
                  ```json
                  ${dataLecturer.value}
                  ```
                - **Majors (Program Studi)**:
                  ```json
                  ${dataMajor.value}
                  ```
                - **Levels (Tingkat)**:
                  ```json
                  ${dataLevel.value}
                  ```
                - **Classes (Kelas)**:
                  ```json
                  ${dataClasses.value}
                  ```

                **Constraints**:
                - Operating hours: 06:00–18:00, Monday to Saturday.
                - Each SKS (credit unit) equals 45 minutes.
                - Check for conflicts where a room (`uidRoom`), class (`uidClasses`), or lecturer (`uidLecturer`) is already scheduled during the requested time and day.
                - If the question involves room availability (e.g., "Is room G205 free on Tuesday from 13:00 to 17:00?"), verify if the room is unoccupied during the specified time by checking existing schedules in Courses.
                - If the question involves lecturer availability (e.g., "Is lecturer Dr. Ahmad free on Tuesday from 13:00 to 17:00?"), verify if the lecturer is unoccupied during the specified time by checking existing schedules linked via `uidLecturer` in Courses.
                - If the question involves course scheduling or academic details (e.g., "Which courses for a specific major are not yet scheduled?" or "What classes have schedules on Tuesday?"), use the `Courses`, `Majors`, `Levels`, and `Classes` data to provide accurate answers.
                - If the room name, lecturer name, major, level, or class in the question does not match any data, return a message indicating that the requested item is not found.
                - Provide a clear, concise answer in Indonesian, avoiding technical jargon unless necessary.

                **Important Output Guidelines**:
                - Do not include any UID values (e.g., `uidLecturer`, `uidRoom`, `uidMajor`, `uidLevel`, `uidClasses`) in the response, as they are internal identifiers.
                - Instead, use descriptive names such as `nameLecturer` for lecturers, `nameRoom` for rooms, `nameMajor` for majors, `nameLevel` for levels, and `nameClasses` for classes when referring to entities in your response.
                - For example, instead of saying "lecturer with uidLecturer 'lect-001' is busy," say "Dosen Dr. Ahmad is busy," using the `nameLecturer` field.
                - You may still use UIDs internally to match data (e.g., linking `uidLecturer` in Courses to Lecturers), but they must not appear in the output.

                **Output**:
                A natural language response in Indonesian, answering the user's question based on the data. Do not return JSON or code unless explicitly requested.
            """.trimIndent()
            getResponseFromAi(aiPrompt)
        } else {
            val aiPrompt = """
                You are a helpful assistant. Answer the user's question in natural, conversational Indonesian. Provide a clear, concise response, avoiding technical jargon unless necessary. Do not use any external data or context unless explicitly provided in the question.

                **User Question**:
                $question

                **Output**:
                A natural language response in Indonesian. Do not return JSON or code unless explicitly requested.
            """.trimIndent()
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


    private fun getAllBuilding() {
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
                Log.e("ViewModeGeminiAI", "Error")
            }
        })
    }

    private fun getAllRoom() {
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
                Log.e("ViewModeGeminiAI", "Error")
            }
        })
    }

    private fun getAllCourse() {
        val ref = FirebaseDatabase.getInstance().getReference("courses")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listSchedule = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getSchedule = dataSnapshot.getValue(DataClassCourse::class.java)
                    getSchedule?.let { listSchedule.add(it) }
                }
                dataCourse.value = listSchedule
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeGeminiAI", "Error")
            }
        })
    }

    private fun getAllLecturer() {
        val ref = FirebaseDatabase.getInstance().getReference("lecturers")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listLecturer = mutableListOf<DataClassLecturer>()
                for (dataSnapshot in snapshot.children) {
                    val getLecturer = dataSnapshot.getValue(DataClassLecturer::class.java)
                    getLecturer?.let { listLecturer.add(it) }
                }
                dataLecturer.value = listLecturer
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeGeminiAI", "Error")
            }
        })
    }

    private fun getAllMajor() {
        val ref = FirebaseDatabase.getInstance().getReference("majors")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val listMajor = mutableListOf<DataClassMajor>()
                for (dataSnapshot in snapshot.children) {
                    val getMajor = dataSnapshot.getValue(DataClassMajor::class.java)
                    getMajor?.let { listMajor.add(it) }
                }
                dataMajor.value = listMajor
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeGeminiAI", "Error")
            }
        })
    }

    private fun getAllLevel() {
        val ref = FirebaseDatabase.getInstance().getReference("levels")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val listLevel = mutableListOf<DataClassLevel>()
                for (dataSnapshot in snapshot.children) {
                    val getLevel = dataSnapshot.getValue(DataClassLevel::class.java)
                    getLevel?.let { listLevel.add(it) }
                }
                dataLevel.value = listLevel
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeGeminiAI", "Error")
            }
        })
    }

    private fun getAllClasses() {
        val ref = FirebaseDatabase.getInstance().getReference("classes")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val listClasses = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getClasses = dataSnapshot.getValue(DataClassCourse::class.java)
                    getClasses?.let { listClasses.add(it) }
                }
                dataClasses.value = listClasses
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeGeminiAI", "Error")
            }
        })
    }
}