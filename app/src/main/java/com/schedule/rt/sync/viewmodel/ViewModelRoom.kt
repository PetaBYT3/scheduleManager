package com.schedule.rt.sync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassRoom

class ViewModelRoom: ViewModel() {

    val databaseReference = FirebaseDatabase.getInstance().getReference("rooms")

    var uidBuilding: String? = null
    var uidRoom: String? = null

    private val _dataRoom = MutableLiveData<List<DataClassRoom>>()
    val dataRoom: LiveData<List<DataClassRoom>> get() = _dataRoom

    fun getRoom() {
        databaseReference.orderByChild("uidBuilding").equalTo(uidBuilding).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listRoom = mutableListOf<DataClassRoom>()
                for (dataSnapshot in snapshot.children) {
                    val getRoom = dataSnapshot.getValue(DataClassRoom::class.java)
                    getRoom?.let { listRoom.add(it) }
                }
                _dataRoom.value = listRoom
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getRoomByUid(uidRoom: String?): LiveData<DataClassRoom?> {
        val dataRoom = MutableLiveData<DataClassRoom?>()
        databaseReference.child(uidRoom.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getRoom = snapshot.getValue(DataClassRoom::class.java)
                dataRoom.value = getRoom
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return dataRoom
    }

    fun addRoom(dataClassRoom: DataClassRoom): LiveData<String?> {
        val addRoomStatus = MutableLiveData<String?>()
        val uidRoom = databaseReference.push().key.toString()
        val nameRoom = dataClassRoom.nameRoom
        dataClassRoom.uidRoom = uidRoom
        dataClassRoom.uidBuilding = uidBuilding
        databaseReference.orderByChild("uidBuilding").equalTo(uidBuilding).addListenerForSingleValueEvent(object : ValueEventListener {
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
                    addRoomStatus.value = "Exist"
                } else {
                    databaseReference.child(uidRoom).setValue(dataClassRoom).addOnSuccessListener {
                        addRoomStatus.value = "Success"
                    }.addOnFailureListener {
                        addRoomStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return addRoomStatus
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
                TODO("Not yet implemented")
            }
        })
        return editRoomStatus
    }

    fun deleteRoom(uidRoom: String?): LiveData<String?> {
        val deleteRoomStatus = MutableLiveData<String?>()
        databaseReference.child(uidRoom.toString()).removeValue().addOnSuccessListener {
            deleteRoomStatus.value = "Success"
        }.addOnFailureListener {
            deleteRoomStatus.value = "Fail"
        }
        return deleteRoomStatus
    }
}