package com.schedule.rt.sync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassLevel

class ViewModelLevel: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("levels")

    var uidMajor: String? = null

    private val _dataLevel = MutableLiveData<List<DataClassLevel>>()
    val dataLevel : LiveData<List<DataClassLevel>> get() = _dataLevel

    fun getLevel() {
        val ref = databaseReference.orderByChild("uidMajor").equalTo(uidMajor)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listLevel = mutableListOf<DataClassLevel>()
                for (dataSnapshot in snapshot.children) {
                    val getLevel = dataSnapshot.getValue(DataClassLevel::class.java)
                    getLevel?.let { listLevel.add(it) }
                }
                _dataLevel.value = listLevel
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getLevelByUid(uidLevel: String?): LiveData<DataClassLevel?> {
        val dataLevel = MutableLiveData<DataClassLevel?>()
        databaseReference.child(uidLevel.toString()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getLevel = snapshot.getValue(DataClassLevel::class.java)
                dataLevel.value = getLevel
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return dataLevel
    }

    fun addLevel(dataClassLevel: DataClassLevel): LiveData<String?> {
        val addLevelStatus = MutableLiveData<String?>()
        val level = dataClassLevel.level
        val uidLevel = databaseReference.push().key.toString()
        dataClassLevel.uidLevel = uidLevel
        dataClassLevel.uidMajor = uidMajor
        val ref = databaseReference.orderByChild("uidMajor").equalTo(uidMajor)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExist = false
                for (child in snapshot.children) {
                    val dataLevel = child.getValue(DataClassLevel::class.java)
                    if (dataLevel?.level == level) {
                        isExist = true
                        break
                    }
                }

                if (isExist == true) {
                    addLevelStatus.value = "Exist"
                } else {
                    databaseReference.child(uidLevel).setValue(dataClassLevel).addOnSuccessListener {
                        addLevelStatus.value = "Success"
                    }.addOnFailureListener {
                        addLevelStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return addLevelStatus
    }

    fun editLevel(dataClassLevel: DataClassLevel): LiveData<String?> {
        val editLevelStatus = MutableLiveData<String?>()
        val level = dataClassLevel.level
        val uidLevel = dataClassLevel.uidLevel.toString()
        val ref = databaseReference.orderByChild("uidMajor").equalTo(uidMajor)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExist = false
                for (child in snapshot.children) {
                    val dataLevel = child.getValue(DataClassLevel::class.java)
                    if (dataLevel?.level == level && dataLevel?.uidLevel != uidLevel) {
                        isExist = true
                        break
                    }
                }

                if (isExist == true) {
                    editLevelStatus.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "level" to level,
                        "semester" to dataClassLevel.semester
                    )
                    databaseReference.child(uidLevel.toString()).updateChildren(updateMap).addOnSuccessListener {
                        editLevelStatus.value = "Success"
                    }.addOnFailureListener {
                        editLevelStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return editLevelStatus
    }

    fun deleteLevel(uidLevel : String?): LiveData<String?> {
        val deleteLevelStatus = MutableLiveData<String?>()

        deleteCourse(uidLevel)
        deleteClasses(uidLevel)

        databaseReference.child(uidLevel.toString()).removeValue().addOnSuccessListener {
            deleteLevelStatus.value = "Success"
        }.addOnFailureListener {
            deleteLevelStatus.value = "Fail"
        }
        return deleteLevelStatus
    }

    private fun deleteClasses(uidLevel: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("classes").orderByChild("uidLevel").equalTo(uidLevel)
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

    private fun deleteCourse(uidLevel: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("courses").orderByChild("uidLevel").equalTo(uidLevel)
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