package com.schedule.rt.sync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassMajor

class ViewModelMajor: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("majors")

    private val _dataMajor = MutableLiveData<List<DataClassMajor>>()
    val dataMajor : LiveData<List<DataClassMajor>> get() = _dataMajor

    fun getMajors() {
        val majorDatabaseReference = FirebaseDatabase.getInstance().getReference("majors")
        majorDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listMajor = mutableListOf<DataClassMajor>()
                for (dataSnapshot in snapshot.children) {
                    val getMajor = dataSnapshot.getValue(DataClassMajor::class.java)
                    getMajor?.let { listMajor.add(it) }
                }
                _dataMajor.value = listMajor
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getMajorByUid(uidMajor: String?): LiveData<DataClassMajor?> {
        val dataMajor = MutableLiveData<DataClassMajor?>()
        databaseReference.child(uidMajor.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getMajor = snapshot.getValue(DataClassMajor::class.java)
                dataMajor.value = getMajor
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return dataMajor
    }

    fun addMajor(dataClassMajor: DataClassMajor): LiveData<String?> {
        val addMajorStatus = MutableLiveData<String?>()
        val uidMajor = databaseReference.push().key.toString()
        val nameMajor = dataClassMajor.nameMajor
        dataClassMajor.uidMajor = uidMajor
        databaseReference.orderByChild("nameMajor").equalTo(nameMajor).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    addMajorStatus.value = "Exist"
                } else {
                    dataClassMajor.uidMajor = uidMajor
                    databaseReference.child(uidMajor).setValue(dataClassMajor).addOnSuccessListener {
                        addMajorStatus.value = "Success"
                    }.addOnFailureListener {
                        addMajorStatus.value = "Fail"
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return addMajorStatus
    }

    fun editMajor(dataClassMajor: DataClassMajor): LiveData<String?> {
        val editMajorStatus = MutableLiveData<String?>()
        val uidMajor = dataClassMajor.uidMajor.toString()
        val nameMajor = dataClassMajor.nameMajor
        databaseReference.orderByChild("nameMajor").equalTo(nameMajor).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    editMajorStatus.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "nameMajor" to nameMajor
                    )
                    databaseReference.child(uidMajor).updateChildren(updateMap).addOnSuccessListener {
                        editMajorStatus.value = "Success"
                    }.addOnFailureListener {
                        editMajorStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return editMajorStatus
    }

    fun deleteMajor(uidMajor: String?): LiveData<String?> {
        val deleteMajorStatus = MutableLiveData<String?>()
        databaseReference.child(uidMajor.toString()).removeValue().addOnSuccessListener {
            deleteMajorStatus.value = "Success"
        }.addOnFailureListener {
            deleteMajorStatus.value = "Fail"
        }
        return deleteMajorStatus
    }
}