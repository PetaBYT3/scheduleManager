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
import com.schedule.rt.sync.dataclass.DataClassMajor
import com.schedule.rt.sync.dataclass.DataClassSchedule

class ViewModelAdministrator: ViewModel() {

    var uidMajor: String? = null

    //Fragment Administrator

    fun getLecturerSize() : LiveData<Int> {
        val lecturerDatabaseReference = FirebaseDatabase.getInstance().getReference("lecturers")
        val lecturerSize = MutableLiveData<Int>()
        lecturerDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lecturerSize.value = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return lecturerSize
    }

    fun getMajorSize() : LiveData<Int> {
        val majorDatabaseReference = FirebaseDatabase.getInstance().getReference("majors")
        val majorSize = MutableLiveData<Int>()
        majorDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                majorSize.value = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return majorSize
    }

    fun getBuildingSize() : LiveData<Int> {
        val buildingDatabaseReference = FirebaseDatabase.getInstance().getReference("buildings")
        val buildingSize = MutableLiveData<Int>()
        buildingDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                buildingSize.value = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return buildingSize
    }

    //Fragment Lecturer



    //Fragment Building



    //Fragment Room

    var uidBuilding : String? = null
    var uidRoom : String? = null



    //Fragment Major

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

    private val _dataMajorByUid = MutableLiveData<DataClassMajor?>()
    val dataMajorByUid: LiveData<DataClassMajor?> get() = _dataMajorByUid

    fun getMajorByUid(uidMajor: String?) {
        val majorDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor")
        majorDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getMajor = snapshot.getValue(DataClassMajor::class.java)
                _dataMajorByUid.value = getMajor
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    val addMajorStatus = MutableLiveData<String?>()

    fun addMajor(dataClassMajor: DataClassMajor) {
        val majorDatabaseReference = FirebaseDatabase.getInstance().getReference("majors")
        val uidMajor = majorDatabaseReference.push().key.toString()
        val nameMajor = dataClassMajor.nameMajor
        dataClassMajor.uidMajor = uidMajor
        majorDatabaseReference.orderByChild("nameMajor").equalTo(nameMajor).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    addMajorStatus.value = "Exist"
                } else {
                    dataClassMajor.uidMajor = uidMajor
                    majorDatabaseReference.child(uidMajor).setValue(dataClassMajor).addOnSuccessListener {
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
    }

    val editMajorStatus = MutableLiveData<String?>()

    fun editMajor(dataClassMajor: DataClassMajor) {
        val majorDatabaseReference = FirebaseDatabase.getInstance().getReference("majors")
        val uidMajor = dataClassMajor.uidMajor.toString()
        val nameMajor = dataClassMajor.nameMajor
        majorDatabaseReference.orderByChild("nameMajor").equalTo(nameMajor).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    editMajorStatus.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "nameMajor" to nameMajor
                    )
                    majorDatabaseReference.child(uidMajor).updateChildren(updateMap).addOnSuccessListener {
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
    }

    val deleteMajorStatus = MutableLiveData<String?>()

    fun deleteMajor(uidMajor: String?) {
        val majorDatabaseReference = FirebaseDatabase.getInstance().getReference("majors")
        majorDatabaseReference.child(uidMajor.toString()).removeValue().addOnSuccessListener {
            deleteMajorStatus.value = "Success"
        }.addOnFailureListener {
            deleteMajorStatus.value = "Fail"
        }
    }

    //Fragment Manager

    private val _dataManager = MutableLiveData<List<DataClassLecturer>>()
    val dataManager: LiveData<List<DataClassLecturer>> get() = _dataManager

    fun getManager() {
        val managerDatabaseReference = FirebaseDatabase.getInstance().getReference("lecturers")
        managerDatabaseReference.orderByChild("uidMajorManager").equalTo(uidMajor).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listLecturer = mutableListOf<DataClassLecturer>()
                for (dataSnapshot in snapshot.children) {
                    val getLecturer = dataSnapshot.getValue(DataClassLecturer::class.java)
                    getLecturer?.let { listLecturer.add(it) }
                }
                _dataManager.value = listLecturer
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    val addManagerStatus = MutableLiveData<String?>()

    fun addManager(uidLecturer: String?) {
        val managerDatabaseReference = FirebaseDatabase.getInstance().getReference("lecturers/$uidLecturer")
        managerDatabaseReference.child("uidMajorManager").setValue(uidMajor).addOnSuccessListener {
            addManagerStatus.value = "Success"
        }.addOnFailureListener {
            addManagerStatus.value = "Fail"
        }
    }

    val deleteManagerStatus = MutableLiveData<String?>()

    fun deleteManager(uidLecturer: String?) {
        val managerDatabaseReference = FirebaseDatabase.getInstance().getReference("lecturers/$uidLecturer")
        managerDatabaseReference.child("uidMajorManager").setValue(null).addOnSuccessListener {
            deleteManagerStatus.value = "Success"
        }.addOnFailureListener {
            deleteManagerStatus.value = "Fail"
        }
    }

    fun getManagerSize(uidMajor: String?): LiveData<Int> {
        val managerDatabaseReference = FirebaseDatabase.getInstance().getReference("lecturers")
        val managerSize = MutableLiveData<Int>()
        managerDatabaseReference.orderByChild("uidMajorManager").equalTo(uidMajor).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                managerSize.value = snapshot.childrenCount.toInt()
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return managerSize
    }

    fun getMajorManager(uidMajor: String?): LiveData<DataClassMajor> {
        val managerDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor")
        val managerMajor = MutableLiveData<DataClassMajor>()
        managerDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                managerMajor.value = snapshot.getValue(DataClassMajor::class.java)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return managerMajor
    }

    //Fragment Day

    fun getDay(day: String?): LiveData<List<DataClassSchedule>> {
        val dayDatabaseReference = FirebaseDatabase.getInstance().getReference("buildings/$uidBuilding/rooms/$uidRoom/$day")
        val dataSchedule = MutableLiveData<List<DataClassSchedule>>()
        dayDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listSchedule = mutableListOf<DataClassSchedule>()
                for (dataSnapshot in snapshot.children) {
                    val getSchedule = dataSnapshot.getValue(DataClassSchedule::class.java)
                    getSchedule?.let { listSchedule.add(it) }
                }
                dataSchedule.value = listSchedule
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return dataSchedule
    }

    fun getSchedule(
        uidMajor: String?,
        uidLevel: String?,
        uidClasses: String?,
        uidCourse: String?
    ): LiveData<DataClassCourse> {
        val scheduleDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes/$uidClasses/courses/$uidCourse")
        val dataCourse = MutableLiveData<DataClassCourse>()
        scheduleDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                dataCourse.value = snapshot.getValue(DataClassCourse::class.java)
                Log.d("TAG", "dataCourse: ${dataCourse.value}")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return dataCourse
    }

    var day : String? = null
    var uidMajorSchedule : String? = null
    var uidLevelSchedule : String? = null
    var uidClassesSchedule : String? = null

    val addScheduleStatus = MutableLiveData<String?>()

    fun addSchedule(uidCourse: String?) {
        val scheduleDatabaseReference = FirebaseDatabase.getInstance().getReference("buildings/$uidBuilding/rooms/$uidRoom/$day")
        val dataSchedule = DataClassSchedule(
            uidMajor = uidMajorSchedule,
            uidLevel = uidLevelSchedule,
            uidClasses = uidClassesSchedule,
            uidCourse = uidCourse
        )
        scheduleDatabaseReference.child(uidCourse.toString()).setValue(dataSchedule).addOnSuccessListener {
            addScheduleStatus.value = "Success"
        }.addOnFailureListener {
            addScheduleStatus.value = "Fail"
        }
    }
}