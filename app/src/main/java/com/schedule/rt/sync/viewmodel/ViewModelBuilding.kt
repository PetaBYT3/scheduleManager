package com.schedule.rt.sync.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassBuilding
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.dataclass.DataClassRoom

class ViewModelBuilding: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("buildings")

    fun getBuilding(): LiveData<List<DataClassBuilding>> {
        val dataBuilding = MutableLiveData<List<DataClassBuilding>>()
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listBuilding = mutableListOf<DataClassBuilding>()
                for (dataSnapshot in snapshot.children) {
                    val getBuilding = dataSnapshot.getValue(DataClassBuilding::class.java)
                    getBuilding?.let { listBuilding.add(it) }
                }
                dataBuilding.value = listBuilding
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeBuilding", "Error")
            }
        })
        return dataBuilding
    }


    fun getBuildingByUid(uidBuilding: String?): LiveData<DataClassBuilding?> {
        val dataBuilding = MutableLiveData<DataClassBuilding?>()
        databaseReference.child(uidBuilding.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getBuilding = snapshot.getValue(DataClassBuilding::class.java)
                dataBuilding.value = getBuilding
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeBuilding", "Error")
            }
        })
        return dataBuilding
    }

    fun addBuilding(dataClassBuilding: DataClassBuilding): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val uidBuilding = databaseReference.push().key.toString()
        val nameBuilding = dataClassBuilding.nameBuilding
        dataClassBuilding.uidBuilding = uidBuilding
        databaseReference.orderByChild("nameBuilding").equalTo(nameBuilding).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    result.value = "Exist"
                } else {
                    databaseReference.child(uidBuilding).setValue(dataClassBuilding).addOnSuccessListener {
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

    fun editBuilding(dataClassBuilding: DataClassBuilding): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val uidBuilding = dataClassBuilding.uidBuilding.toString()
        val nameBuilding = dataClassBuilding.nameBuilding
        databaseReference.orderByChild("nameBuilding").equalTo(nameBuilding).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    result.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "nameBuilding" to nameBuilding
                    )
                    databaseReference.child(uidBuilding).updateChildren(updateMap).addOnSuccessListener {
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

    fun deleteBuilding(uidBuilding: String?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val courseRef = FirebaseDatabase.getInstance().getReference("courses").orderByChild("uidBuilding").equalTo(uidBuilding)
        courseRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot in snapshot.children) {
                    val getCourse = snapshot.getValue(DataClassCourse::class.java)
                    if (getCourse?.uidBuilding == uidBuilding) {
                        val updateChildren = mapOf(
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
                val roomRef = FirebaseDatabase.getInstance().getReference("rooms").orderByChild("uidBuilding").equalTo(uidBuilding)
                roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (dataSnapshot in snapshot.children) {
                            val getRoom = dataSnapshot.getValue(DataClassRoom::class.java)
                            if (getRoom?.uidBuilding == uidBuilding) {
                                dataSnapshot.ref.removeValue()
                            }
                        }
                        databaseReference.child(uidBuilding.toString()).removeValue().addOnSuccessListener {
                            result.value = "Success"
                        }.addOnFailureListener {
                            result.value = "Fail"
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        result.value = "Error"
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                result.value = "Error"
            }
        })
        return result
    }
}