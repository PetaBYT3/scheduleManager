package com.schedule.rt.sync.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassClasses

class ViewModelClasses: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("classes")

    private val _uidMajorReference = MutableLiveData<String?>()
    val uidMajorReference: LiveData<String?> get() = _uidMajorReference

    fun uidMajorReference(uidMajor: String?) {
        _uidMajorReference.value = uidMajor
    }

    private val _uidLevelReference = MutableLiveData<String?>()
    val uidLevelReference: LiveData<String?> = _uidLevelReference

    fun uidLevelReference(uidLevel: String?) {
        _uidLevelReference.value = uidLevel
    }

    fun getClasses(): LiveData<List<DataClassClasses>> {
        val dataClasses = MutableLiveData<List<DataClassClasses>>()
        val ref = databaseReference.orderByChild("uidLevel").equalTo(uidLevelReference.value)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listClasses = mutableListOf<DataClassClasses>()
                for (dataSnapshot in snapshot.children) {
                    val getClasses = dataSnapshot.getValue(DataClassClasses::class.java)
                    getClasses?.let { listClasses.add(it) }
                }
                dataClasses.value = listClasses
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeClasses", "Error")
            }
        })
        return dataClasses
    }

    fun getClassesByUid(uidClasses: String?): LiveData<DataClassClasses?> {
        val dataClassClasses = MutableLiveData<DataClassClasses?>()
        val ref = databaseReference.child(uidClasses.toString())
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val dataClasses = snapshot.getValue(DataClassClasses::class.java)
                dataClassClasses.value = dataClasses
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeClasses", "Error")
            }
        })

        return dataClassClasses
    }

    fun addClasses(dataClassClasses: DataClassClasses): LiveData<String?> {
        val addClassesStatus = MutableLiveData<String?>()
        val uidClasses = databaseReference.push().key.toString()
        val nameClasses = dataClassClasses.nameClasses
        dataClassClasses.uidMajor = uidMajorReference.value
        dataClassClasses.uidLevel = uidLevelReference.value
        dataClassClasses.uidClasses = uidClasses
        val ref = databaseReference.orderByChild("uidLevel").equalTo(uidLevelReference.value)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExist = false
                for (child in snapshot.children) {
                    val dataClasses = child.getValue(DataClassClasses::class.java)
                    if (dataClasses?.nameClasses == nameClasses) {
                        isExist = true
                        break
                    }
                }

                if (isExist == true) {
                    addClassesStatus.value = "Exist"
                } else {
                    databaseReference.child(uidClasses).setValue(dataClassClasses).addOnSuccessListener {
                        addClassesStatus.value = "Success"
                    }.addOnFailureListener {
                        addClassesStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                addClassesStatus.value = "Error"
            }
        })
        return addClassesStatus
    }

    fun editClasses(dataClassClasses: DataClassClasses): LiveData<String?> {
        val editClassesStatus = MutableLiveData<String?>()
        val uidClasses = dataClassClasses.uidClasses.toString()
        val nameClasses = dataClassClasses.nameClasses.toString()
        val ref = databaseReference.orderByChild("uidLevel").equalTo(uidLevelReference.value)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExist = false
                for (child in snapshot.children) {
                    val dataClasses = child.getValue(DataClassClasses::class.java)
                    if (dataClasses?.nameClasses == nameClasses && dataClasses.uidClasses != uidClasses) {
                        isExist = true
                        break
                    }
                }

                if (isExist == true) {
                    editClassesStatus.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "nameClasses" to nameClasses,
                    )
                    databaseReference.child(uidClasses).updateChildren(updateMap).addOnSuccessListener {
                        editClassesStatus.value = "Success"
                    }.addOnFailureListener {
                        editClassesStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                editClassesStatus.value = "Error"
            }
        })
        return editClassesStatus
    }

    fun deleteClasses(uidClasses : String?): LiveData<String?> {
        val deleteClassesStatus = MutableLiveData<String?>()

        deleteCourse(uidClasses)
        deleteUserData(uidClasses)

        databaseReference.child(uidClasses.toString()).removeValue().addOnSuccessListener {
            deleteClassesStatus.value = "Success"
        }.addOnFailureListener {
            deleteClassesStatus.value = "Fail"
        }
        return deleteClassesStatus
    }

    private fun deleteCourse(uidClasses: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("courses").orderByChild("uidClasses").equalTo(uidClasses)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        child.ref.removeValue()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeClasses", "Error")
            }
        })
    }

    private fun deleteUserData(uidClasses: String?) {
        val ref = FirebaseDatabase.getInstance().getReference("users").orderByChild("uidClasses").equalTo(uidClasses)
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val updateChildren = mapOf(
                            "uidMajor" to null,
                            "uidLevel" to null,
                            "uidClasses" to null
                        )
                        child.ref.updateChildren(updateChildren)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeClasses", "Error")
            }
        })
    }

    fun getClassesSizeByLevel(uidLevel: String?): LiveData<Int?> {
        val classesSize = MutableLiveData<Int?>()
        val ref = databaseReference.orderByChild("uidLevel").equalTo(uidLevel)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                classesSize.value = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeClasses", "Error")
            }
        })

        return classesSize
    }

    fun getClassesSizeByMajor(uidMajor: String?): LiveData<Int> {
        val classesSize = MutableLiveData<Int>()
        val ref = databaseReference.orderByChild("uidMajor").equalTo(uidMajor)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val size = snapshot.childrenCount.toInt()
                classesSize.value = size
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeClasses", "Error")
            }
        })
        return classesSize
    }
}