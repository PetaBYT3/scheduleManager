package com.schedule.rt.sync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassBuilding

class ViewModelBuilding: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("buildings")

    private val _dataBuilding = MutableLiveData<List<DataClassBuilding>>()
    val dataBuilding: LiveData<List<DataClassBuilding>> get() = _dataBuilding

    fun getBuilding() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listBuilding = mutableListOf<DataClassBuilding>()
                for (dataSnapshot in snapshot.children) {
                    val getBuilding = dataSnapshot.getValue(DataClassBuilding::class.java)
                    getBuilding?.let { listBuilding.add(it) }
                }
                _dataBuilding.value = listBuilding
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    fun getBuildingByUid(uidBuilding: String?): LiveData<DataClassBuilding?> {
        val dataBuilding = MutableLiveData<DataClassBuilding?>()
        databaseReference.child(uidBuilding.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getBuilding = snapshot.getValue(DataClassBuilding::class.java)
                dataBuilding.value = getBuilding
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return dataBuilding
    }

    fun addBuilding(dataClassBuilding: DataClassBuilding): LiveData<String?> {
        val addBuildingStatus = MutableLiveData<String?>()
        val uidBuilding = databaseReference.push().key.toString()
        val nameBuilding = dataClassBuilding.nameBuilding
        dataClassBuilding.uidBuilding = uidBuilding
        databaseReference.orderByChild("nameBuilding").equalTo(nameBuilding).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    addBuildingStatus.value = "Exist"
                } else {
                    databaseReference.child(uidBuilding).setValue(dataClassBuilding).addOnSuccessListener {
                        addBuildingStatus.value = "Success"
                    }.addOnFailureListener {
                        addBuildingStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return addBuildingStatus
    }

    fun editBuilding(dataClassBuilding: DataClassBuilding): LiveData<String?> {
        val editBuildingStatus = MutableLiveData<String?>()
        val uidBuilding = dataClassBuilding.uidBuilding.toString()
        val nameBuilding = dataClassBuilding.nameBuilding
        databaseReference.orderByChild("nameBuilding").equalTo(nameBuilding).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    editBuildingStatus.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "nameBuilding" to nameBuilding
                    )
                    databaseReference.child(uidBuilding).updateChildren(updateMap).addOnSuccessListener {
                        editBuildingStatus.value = "Success"
                    }.addOnFailureListener {
                        editBuildingStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return editBuildingStatus
    }

    fun deleteBuilding(uidBuilding: String?): LiveData<String?> {
        val deleteBuildingStatus = MutableLiveData<String?>()
        val roomDatabaseReference = FirebaseDatabase.getInstance().getReference("rooms").orderByChild("uidBuilding").equalTo(uidBuilding)
        databaseReference.child(uidBuilding.toString()).removeValue().addOnSuccessListener {
            roomDatabaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        dataSnapshot.ref.setValue(null)
                    }
                    deleteBuildingStatus.value = "Success"
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })

        }.addOnFailureListener {
            deleteBuildingStatus.value = "Fail"
        }
        return deleteBuildingStatus
    }

    fun getRoomSize(uidBuilding: String?): LiveData<Int> {
        val roomSize = MutableLiveData<Int>()
        val roomDatabaseReference = FirebaseDatabase.getInstance().getReference("rooms").orderByChild("uidBuilding").equalTo(uidBuilding)
        roomDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                roomSize.value = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return roomSize
    }
}