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
import com.schedule.rt.sync.dataclass.DataClassRoom

class ViewModelRoom: ViewModel() {

    val databaseReference = FirebaseDatabase.getInstance().getReference("rooms")

    private val _uidBuildingReference = MutableLiveData<String?>()
    val uidBuildingReference: LiveData<String?> get() = _uidBuildingReference

    fun uidBuildingReference(uidBuilding: String?) {
        _uidBuildingReference.value = uidBuilding
    }

    fun getAllRoom(): LiveData<List<DataClassRoom>> {
        val dataRoom = MutableLiveData<List<DataClassRoom>>()
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val listRoom = mutableListOf<DataClassRoom>()
                for (dataSnapshot in snapshot.children) {
                    val getRoom = dataSnapshot.getValue(DataClassRoom::class.java)
                    getRoom?.let { listRoom.add(it) }
                }
                dataRoom.value = listRoom
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelRoom", "Error")
            }
        })

        return dataRoom
    }

    fun getRoom(): LiveData<List<DataClassRoom>> {
        val dataRoom = MutableLiveData<List<DataClassRoom>>()
        databaseReference.orderByChild("uidBuilding").equalTo(uidBuildingReference.value).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listRoom = mutableListOf<DataClassRoom>()
                for (dataSnapshot in snapshot.children) {
                    val getRoom = dataSnapshot.getValue(DataClassRoom::class.java)
                    getRoom?.let { listRoom.add(it) }
                }
                dataRoom.value = listRoom
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelRoom", "Error")
            }
        })
        return dataRoom
    }

    fun getRoomByUid(uidRoom: String?): LiveData<DataClassRoom?> {
        val dataRoom = MutableLiveData<DataClassRoom?>()
        databaseReference.child(uidRoom.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getRoom = snapshot.getValue(DataClassRoom::class.java)
                dataRoom.value = getRoom
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelRoom", "Error")
            }
        })
        return dataRoom
    }

    fun addRoom(dataClassRoom: DataClassRoom): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val uidRoom = databaseReference.push().key.toString()
        val nameRoom = dataClassRoom.nameRoom
        dataClassRoom.uidRoom = uidRoom
        dataClassRoom.uidBuilding = uidBuildingReference.value
        databaseReference.orderByChild("uidBuilding").equalTo(uidBuildingReference.value).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExist = false
                for (child in snapshot.children) {
                    val getRoom = child.getValue(DataClassRoom::class.java)
                    if (getRoom?.nameRoom == nameRoom) {
                        isExist = true
                        break
                    }
                }

                if (isExist == true) {
                    result.value = "Exist"
                } else {
                    databaseReference.child(uidRoom).setValue(dataClassRoom).addOnSuccessListener {
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

    fun editRoom(dataClassRoom: DataClassRoom): LiveData<String?> {
        val editRoomStatus = MutableLiveData<String?>()
        val uidRoom = dataClassRoom.uidRoom.toString()
        val nameRoom = dataClassRoom.nameRoom
        databaseReference.orderByChild("nameRoom").equalTo(nameRoom).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    editRoomStatus.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "nameRoom" to nameRoom
                    )
                    databaseReference.child(uidRoom).updateChildren(updateMap).addOnSuccessListener {
                        editRoomStatus.value = "Success"
                    }.addOnFailureListener {
                        editRoomStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                editRoomStatus.value = "Error"
            }
        })
        return editRoomStatus
    }

    fun deleteRoom(uidRoom: String?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val courseRef = FirebaseDatabase.getInstance().getReference("courses").orderByChild("uidRoom").equalTo(uidRoom)
        courseRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapshot in snapshot.children) {
                    val getCourse = snapshot.getValue(DataClassCourse::class.java)
                    if (getCourse?.uidRoom == uidRoom) {
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
                databaseReference.child(uidRoom.toString()).removeValue().addOnSuccessListener {
                    result.value = "Success"
                }.addOnFailureListener {
                    result.value = "Fail"
                }
            }
            override fun onCancelled(error: DatabaseError) {
                result.value = "Error"
            }
        })
        return result
    }

    fun getRoomSizeByBuilding(uidBuilding: String?): LiveData<Int> {
        val roomSize = MutableLiveData<Int>()
        databaseReference.orderByChild("uidBuilding").equalTo(uidBuilding).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                roomSize.value = snapshot.childrenCount.toInt()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModelRoom", "Error")
            }
        })
        return roomSize
    }
}