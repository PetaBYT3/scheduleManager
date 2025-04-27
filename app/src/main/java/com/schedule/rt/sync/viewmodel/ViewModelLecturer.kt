package com.schedule.rt.sync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassLecturer

class ViewModelLecturer: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("lecturers")

    private val _dataLecturer = MutableLiveData<List<DataClassLecturer>>()
    val dataLecturer: LiveData<List<DataClassLecturer>> get() = _dataLecturer

    fun getLecturer() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listLecturer = mutableListOf<DataClassLecturer>()
                for (dataSnapshot in snapshot.children) {
                    val getLecturer = dataSnapshot.getValue(DataClassLecturer::class.java)
                    getLecturer?.let { listLecturer.add(it) }
                }
                _dataLecturer.value = listLecturer
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getLecturerByUid(uidLecturer: String?): LiveData<DataClassLecturer?> {
        val dataLecturer = MutableLiveData<DataClassLecturer?>()
        databaseReference.child(uidLecturer.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getLecturer = snapshot.getValue(DataClassLecturer::class.java)
                dataLecturer.value = getLecturer
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return dataLecturer
    }

    fun addLecturer(dataClassLecturer: DataClassLecturer): LiveData<String?> {
        val addLecturerStatus = MutableLiveData<String?>()
        val uidLecturer = databaseReference.push().key.toString()
        val nikLecturer = dataClassLecturer.nikLecturer
        dataClassLecturer.uidLecturer = uidLecturer
        databaseReference.orderByChild("nikLecturer").equalTo(nikLecturer).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    addLecturerStatus.value = "Nik Exist"
                } else {
                    databaseReference.child(uidLecturer).setValue(dataClassLecturer).addOnSuccessListener {
                        addLecturerStatus.value = "Success"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return addLecturerStatus
    }

    fun editLecturer(dataClassLecturer: DataClassLecturer): LiveData<String?> {
        val editLecturerStatus = MutableLiveData<String?>()
        val uidLecturer = dataClassLecturer.uidLecturer.toString()
        val nameLecturer = dataClassLecturer.nameLecturer
        val nikLecturer = dataClassLecturer.nikLecturer
        val administratorAccess = dataClassLecturer.administratorAccess
        databaseReference.orderByChild("nikLecturer").equalTo(nikLecturer).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isNikExist = false
                for (dataSnapshot in snapshot.children) {
                    val lecturer = dataSnapshot.getValue(DataClassLecturer::class.java)
                    if (lecturer?.uidLecturer != null && lecturer.uidLecturer != uidLecturer) {
                        isNikExist = true
                        break
                    }
                }
                if (isNikExist) {
                    editLecturerStatus.value = "Nik Exist"
                } else {
                    val updateMap = mapOf(
                        "nameLecturer" to nameLecturer,
                        "nikLecturer" to nikLecturer,
                        "administratorAccess" to administratorAccess
                    )
                    databaseReference.child(uidLecturer).updateChildren(updateMap).addOnSuccessListener {
                        editLecturerStatus.value = "Success"
                    }.addOnFailureListener {
                        editLecturerStatus.value = "Fail"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return editLecturerStatus
    }

    fun deleteLecturer(uidLecturer: String?): LiveData<String?> {
        val deleteLecturerStatus = MutableLiveData<String?>()
        databaseReference.child(uidLecturer.toString()).removeValue().addOnSuccessListener {
            deleteLecturerStatus.value = "Success"
        }.addOnFailureListener {
            deleteLecturerStatus.value = "Fail"
        }
        return deleteLecturerStatus
    }
}