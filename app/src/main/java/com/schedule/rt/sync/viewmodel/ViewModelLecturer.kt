package com.schedule.rt.sync.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.dataclass.DataClassLecturer
import com.schedule.rt.sync.dataclass.DataClassUser

class ViewModelLecturer: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("lecturers")

    private val _uidLecturer = MutableLiveData<String?>()
    val uidLecturer: LiveData<String?> get() = _uidLecturer

    fun sendLecturerUid(uidLecturer: String?) {
        _uidLecturer.value = uidLecturer
    }

    fun getLecturer(): LiveData<List<DataClassLecturer>> {
        val dataLecturer = MutableLiveData<List<DataClassLecturer>>()
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listLecturer = mutableListOf<DataClassLecturer>()
                for (dataSnapshot in snapshot.children) {
                    val getLecturer = dataSnapshot.getValue(DataClassLecturer::class.java)
                    getLecturer?.let { listLecturer.add(it) }
                }
                dataLecturer.value = listLecturer
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelLecturer", "Error")
            }
        })
        return dataLecturer
    }

    fun getLecturerByManager(uidMajor: String?): LiveData<List<DataClassLecturer>> {
        val dataLecturer = MutableLiveData<List<DataClassLecturer>>()
        val ref = databaseReference.orderByChild("uidMajorManager").equalTo(uidMajor)
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
                Log.e("ViewModelLecturer", "Error")
            }
        })

        return dataLecturer
    }

    fun getLecturerByUid(uidLecturer: String?): LiveData<DataClassLecturer?> {
        val dataLecturer = MutableLiveData<DataClassLecturer?>()
        databaseReference.child(uidLecturer.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getLecturer = snapshot.getValue(DataClassLecturer::class.java)
                dataLecturer.value = getLecturer
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelLecturer", "Error")
            }
        })
        return dataLecturer
    }

    fun addLecturer(dataClassLecturer: DataClassLecturer): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val uidLecturer = databaseReference.push().key.toString()
        val nikLecturer = dataClassLecturer.nikLecturer
        dataClassLecturer.uidLecturer = uidLecturer
        databaseReference.orderByChild("nikLecturer").equalTo(nikLecturer).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    result.value = "Exist"
                } else {
                    databaseReference.child(uidLecturer).setValue(dataClassLecturer).addOnSuccessListener {
                        result.value = "Success"
                    }.addOnFailureListener {
                        result.value = "Fail"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                result.value = "Error"
            }
        })
        return result
    }

    fun editLecturer(dataClassLecturer: DataClassLecturer): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val uidLecturer = dataClassLecturer.uidLecturer.toString()
        val nameLecturer = dataClassLecturer.nameLecturer
        val nikLecturer = dataClassLecturer.nikLecturer
        val administratorAccess = dataClassLecturer.administratorAccess
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isNikExist = false
                for (child in snapshot.children) {
                    val getLecturer = child.getValue(DataClassLecturer::class.java)
                    if (getLecturer?.uidLecturer != uidLecturer && getLecturer?.nikLecturer == nikLecturer) {
                        isNikExist = true
                        break
                    }
                }

                if (isNikExist == true) {
                    result.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "nameLecturer" to nameLecturer,
                        "nikLecturer" to nikLecturer,
                        "administratorAccess" to administratorAccess
                    )
                    databaseReference.child(uidLecturer).updateChildren(updateMap).addOnSuccessListener {
                        result.value = "Success"
                    }.addOnFailureListener {
                        result.value = "Fail"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                result.value = "Error"
            }
        })
        return result
    }

    fun deleteLecturer(uidLecturer: String?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val courseRef = FirebaseDatabase.getInstance().getReference("courses").orderByChild("uidLecturer").equalTo(uidLecturer)
        courseRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot in snapshot.children) {
                    val getCourse = snapshot.getValue(DataClassCourse::class.java)
                    if (getCourse?.uidLecturer == uidLecturer) {
                        val updateChildren = mapOf(
                            "uidLecturer" to null,
                            "day" to null,
                            "startTime" to null,
                            "endTime" to null,
                            "uidBuilding" to null,
                            "uidRoom" to null,
                            "uidRoomDay" to null
                        )
                        snapshot.ref.updateChildren(updateChildren)
                    }
                }

                val userRef = FirebaseDatabase.getInstance().getReference("users").orderByChild("uidLecturer").equalTo(uidLecturer)
                userRef.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dataSnapshot in snapshot.children) {
                            val getUser = dataSnapshot.getValue(DataClassUser::class.java)
                            Log.d("dataUser", getUser.toString())
                            if (getUser?.uidLecturer == uidLecturer) {
                                val updateChildren = mapOf(
                                    "uidLecturer" to null
                                )
                                dataSnapshot.ref.updateChildren(updateChildren)
                            }
                        }
                        databaseReference.child(uidLecturer.toString()).removeValue().addOnSuccessListener {
                            result.value = "Success"
                        }.addOnFailureListener {
                            result.value = "Fail"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                result.value = "Error"
            }
        })
        return result
    }

    fun addLecturerManager(uidLecturer: String?, uidMajor: String?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val updateChildren = mapOf(
            "uidMajorManager" to uidMajor
        )
        databaseReference.child(uidLecturer.toString()).updateChildren(updateChildren).addOnSuccessListener {
            result.value = "Success"
        }.addOnFailureListener {
            result.value = "Fail"
        }

        return result
    }

    fun deleteLecturerManager(uidLecturer: String?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val updateChildren = mapOf(
            "uidMajorManager" to null
        )
        databaseReference.child(uidLecturer.toString()).updateChildren(updateChildren).addOnSuccessListener {
            result.value = "Success"
        }.addOnFailureListener {
            result.value = "Fail"
        }
        return result
    }

    fun getLecturerSizeByMajor(uidMajor: String?): LiveData<Int> {
        val lecturerSize = MutableLiveData<Int>()
        val ref = databaseReference.orderByChild("uidMajorManager").equalTo(uidMajor)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lecturerSize.value = snapshot.childrenCount.toInt()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelLecturer", "Error")
            }
        })
        return lecturerSize
    }
}