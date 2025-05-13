package com.schedule.rt.sync.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassLecturer
import com.schedule.rt.sync.dataclass.DataClassMajor

class ViewModelMajor: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("majors")

    fun getMajors(): LiveData<List<DataClassMajor>> {
        val dataMajor = MutableLiveData<List<DataClassMajor>>()
        val majorDatabaseReference = FirebaseDatabase.getInstance().getReference("majors")
        majorDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listMajor = mutableListOf<DataClassMajor>()
                for (dataSnapshot in snapshot.children) {
                    val getMajor = dataSnapshot.getValue(DataClassMajor::class.java)
                    getMajor?.let { listMajor.add(it) }
                }
                dataMajor.value = listMajor
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return dataMajor
    }

    fun getMajorByUid(uidMajor: String?): LiveData<DataClassMajor?> {
        val dataMajor = MutableLiveData<DataClassMajor?>()
        databaseReference.child(uidMajor.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getMajor = snapshot.getValue(DataClassMajor::class.java)
                dataMajor.value = getMajor
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelMajor", "onCancelled: ${error.message}")
            }
        })
        return dataMajor
    }

    fun addMajor(dataClassMajor: DataClassMajor): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val uidMajor = databaseReference.push().key.toString()
        val nameMajor = dataClassMajor.nameMajor
        dataClassMajor.uidMajor = uidMajor
        databaseReference.orderByChild("nameMajor").equalTo(nameMajor).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    result.value = "Exist"
                } else {
                    dataClassMajor.uidMajor = uidMajor
                    databaseReference.child(uidMajor).setValue(dataClassMajor).addOnSuccessListener {
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

    fun editMajor(dataClassMajor: DataClassMajor): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val uidMajor = dataClassMajor.uidMajor.toString()
        val nameMajor = dataClassMajor.nameMajor
        databaseReference.orderByChild("nameMajor").equalTo(nameMajor).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    result.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "nameMajor" to nameMajor
                    )
                    databaseReference.child(uidMajor).updateChildren(updateMap).addOnSuccessListener {
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

    fun deleteMajor(uidMajor: String?): LiveData<String?> {
        val deleteMajorStatus = MutableLiveData<String?>()

        deleteLevel(uidMajor)
        deleteClasses(uidMajor)
        deleteCourse(uidMajor)

        val lecturerRef = FirebaseDatabase.getInstance().getReference("lecturers").orderByChild("uidMajorManager").equalTo(uidMajor)
        lecturerRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot in snapshot.children) {
                    val dataLecturer = snapshot.getValue(DataClassLecturer::class.java)
                    if (dataLecturer?.uidMajorManager == uidMajor) {
                        val updateChildren = mapOf(
                            "uidMajorManager" to null
                        )
                        snapshot.ref.updateChildren(updateChildren).addOnSuccessListener {
                            deleteMajorStatus.value = "Success"
                        }.addOnFailureListener {
                            deleteMajorStatus.value = "Fail"
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        databaseReference.child(uidMajor.toString()).removeValue().addOnSuccessListener {
            deleteMajorStatus.value = "Success"
        }.addOnFailureListener {
            deleteMajorStatus.value = "Fail"
        }
        return deleteMajorStatus
    }

    private fun deleteLevel(uidMajor: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("levels").orderByChild("uidMajor").equalTo(uidMajor)
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun deleteClasses(uidMajor: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("classes").orderByChild("uidMajor").equalTo(uidMajor)
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun deleteCourse(uidMajor: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("courses").orderByChild("uidMajor").equalTo(uidMajor)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}