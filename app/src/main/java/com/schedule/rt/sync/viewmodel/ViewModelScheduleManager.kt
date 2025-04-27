package com.schedule.rt.sync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassClasses
import com.schedule.rt.sync.dataclass.DataClassCourse
import com.schedule.rt.sync.dataclass.DataClassLevel
import com.schedule.rt.sync.dataclass.DataClassMajor

class ViewModelScheduleManager: ViewModel() {

    var uidMajor: String? = null
    var uidLevel: String? = null
    var uidClasses: String? = null

    //Fragment Level

    fun getMajorByUid(): LiveData<DataClassMajor?> {
        val majorDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor")
        val dataMajor = MutableLiveData<DataClassMajor?>()
        majorDatabaseReference.addValueEventListener(object : ValueEventListener {
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

    private val _dataLevel = MutableLiveData<List<DataClassLevel>>()
    val dataLevel : LiveData<List<DataClassLevel>> get() = _dataLevel

    fun getLevel() {
        val levelDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels")
        levelDatabaseReference.addValueEventListener(object : ValueEventListener {
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
        val levelDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel")
        val dataLevel = MutableLiveData<DataClassLevel?>()
        levelDatabaseReference.addValueEventListener(object : ValueEventListener {
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

    val addLevelStatus = MutableLiveData<String?>()

    fun addLevel(dataClassLevel: DataClassLevel) {
        val levelDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/")
        val uid = levelDatabaseReference.push().key.toString()
        val level = dataClassLevel.level
        dataClassLevel.uidLevel = uid
        levelDatabaseReference.orderByChild("level").equalTo(level).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    addLevelStatus.value = "Exist"
                } else {
                    levelDatabaseReference.child(uid).setValue(dataClassLevel).addOnSuccessListener {
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
    }

    val editLevelStatus = MutableLiveData<String?>()

    fun editLevel(uidLevel : String?, dataClassLevel: DataClassLevel) {
        val level = dataClassLevel.level
        val levelDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels")
        levelDatabaseReference.orderByChild("level").equalTo(level).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExist = false
                for (child in snapshot.children) {
                    val existingUid = child.child("uidLevel").getValue(String::class.java)
                    if (existingUid != uidLevel) {
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
                    levelDatabaseReference.child(uidLevel.toString()).updateChildren(updateMap).addOnSuccessListener {
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
    }

    val deleteLevelStatus = MutableLiveData<String?>()

    fun deleteLevel(uidLevel : String?) {
        val levelDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel")
        levelDatabaseReference.removeValue().addOnSuccessListener {
            deleteLevelStatus.value = "Success"
        }.addOnFailureListener {
            deleteLevelStatus.value = "Fail"
        }
    }

    fun getClassesSize(uidLevel: String?): LiveData<Int> {
        val levelDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes")
        val classesSize = MutableLiveData<Int>()
        levelDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                classesSize.value = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return classesSize
    }

    //Fragment Classes

    private val _dataClassesByUid = MutableLiveData<DataClassClasses?>()
    val dataClassesByUid : LiveData<DataClassClasses?> get() = _dataClassesByUid

    fun getClassesByUid(uidClasses : String?) {
        val classesDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes/$uidClasses")
        classesDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getClasses = snapshot.getValue(DataClassClasses::class.java)
                _dataClassesByUid.value = getClasses
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private val _dataClasses = MutableLiveData<List<DataClassClasses>>()
    val dataClasses : LiveData<List<DataClassClasses>> get() = _dataClasses

    fun getClasses() {
        val classesDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes")
        classesDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listClasses = mutableListOf<DataClassClasses>()
                for (dataSnapshot in snapshot.children) {
                    val getClasses = dataSnapshot.getValue(DataClassClasses::class.java)
                    getClasses?.let { listClasses.add(it) }
                }
                _dataClasses.value = listClasses
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun getClasses1(uidLevel: String?): LiveData<List<DataClassClasses>> {
        val classesDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes")
        val mutableLiveData = MutableLiveData<List<DataClassClasses>>()
        classesDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listClasses = mutableListOf<DataClassClasses>()
                for (dataSnapshot in snapshot.children) {
                    val getClasses = dataSnapshot.getValue(DataClassClasses::class.java)
                    getClasses?.let { listClasses.add(it) }
                }
                mutableLiveData.value = listClasses
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return mutableLiveData
    }

    val addClassesStatus = MutableLiveData<String?>()

    fun addClasses(dataClassClasses: DataClassClasses) {
        val classesDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes")
        val uid = classesDatabaseReference.push().key.toString()
        val nameClasses = dataClassClasses.nameClasses
        dataClassClasses.uidClasses = uid
        classesDatabaseReference.orderByChild("nameClasses").equalTo(nameClasses).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    addClassesStatus.value = "Exist"
                } else {
                    classesDatabaseReference.child(uid).setValue(dataClassClasses).addOnSuccessListener {
                        addClassesStatus.value = "Success"
                    }.addOnFailureListener {
                        addClassesStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    val editClassesStatus = MutableLiveData<String?>()

    fun editClasses(dataClassClasses: DataClassClasses) {
        val uid = dataClassClasses.uidClasses.toString()
        val nameClasses = dataClassClasses.nameClasses.toString()
        val classesDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes")
        classesDatabaseReference.orderByChild("nameClasses").equalTo(nameClasses).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    editClassesStatus.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "nameClasses" to nameClasses,
                    )
                    classesDatabaseReference.child(uid).updateChildren(updateMap).addOnSuccessListener {
                        editClassesStatus.value = "Success"
                    }.addOnFailureListener {
                        editClassesStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    val deleteClassesStatus = MutableLiveData<String?>()

    fun deleteClasses(uidClasses : String?) {
        val classesDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes")
        classesDatabaseReference.child(uidClasses.toString()).removeValue().addOnSuccessListener {
            deleteClassesStatus.value = "Success"
        }.addOnFailureListener {
            deleteClassesStatus.value = "Fail"
        }
    }

    fun getCourseSize(uidClasses: String?) : LiveData<Int> {
        val courseDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes/$uidClasses/courses")
        val courseSize = MutableLiveData<Int>()
        courseDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                courseSize.value = snapshot.childrenCount.toInt()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return courseSize
    }

    //ViewModel Fragment Course

    private val _dataCourse = MutableLiveData<List<DataClassCourse>>()
    val dataCourse: LiveData<List<DataClassCourse>> get() =  _dataCourse

    fun getCourse() {
        val courseDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes/$uidClasses/courses")
        courseDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listCourse = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getCourse = dataSnapshot.getValue(DataClassCourse::class.java)
                    getCourse?.let { listCourse.add(it) }
                }
                _dataCourse.value = listCourse
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private val _dataCourseByUid = MutableLiveData<DataClassCourse?>()
    val dataCourseByUid: LiveData<DataClassCourse?> get() =  _dataCourseByUid

    fun getCourseByUid(uidCourse : String) {
        val courseDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes/$uidClasses/courses/$uidCourse")
        courseDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getCourse = snapshot.getValue(DataClassCourse::class.java)
                _dataCourseByUid.value = getCourse
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    val addCourseStatus = MutableLiveData<String?>()

    fun addCourse(dataClassCourse: DataClassCourse) {
        val courseDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes/$uidClasses/courses")
        val uid = courseDatabaseReference.push().key.toString()
        val nameCourse = dataClassCourse.nameCourse
        dataClassCourse.uidCourse = uid
        courseDatabaseReference.orderByChild("nameCourse").equalTo(nameCourse).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    addCourseStatus.value = "Exist"
                } else {
                    courseDatabaseReference.child(uid).setValue(dataClassCourse).addOnSuccessListener {
                        addCourseStatus.value = "Success"
                    }.addOnFailureListener {
                        addCourseStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    val editCourseStatus = MutableLiveData<String?>()

    fun editCourse(dataClassCourse: DataClassCourse) {
        val courseDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes/$uidClasses/courses")
        val uid = dataClassCourse.uidCourse.toString()
        val nameCourse = dataClassCourse.nameCourse
        courseDatabaseReference.orderByChild("nameCourse").equalTo(nameCourse).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExist = false
                for (child in snapshot.children) {
                    val existingUid = child.child("uidCourse").getValue(String::class.java)
                    if (existingUid != dataClassCourse.uidCourse) {
                        isExist = true
                        break
                    }
                }
                if (isExist == true) {
                    editCourseStatus.value = "Exist"
                } else {
                    val updateMap = mapOf(
                        "nameCourse" to nameCourse,
                        "sksCourse" to dataClassCourse.sksCourse,
                    )
                    courseDatabaseReference.child(uid).updateChildren(updateMap).addOnSuccessListener {
                        editCourseStatus.value = "Success"
                    }.addOnFailureListener {
                        editCourseStatus.value = "Fail"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    val deleteCourseStatus = MutableLiveData<String?>()

    fun deleteCourse(uidCourse : String) {
        val courseDatabaseReference = FirebaseDatabase.getInstance().getReference("majors/$uidMajor/levels/$uidLevel/classes/$uidClasses/courses")
        courseDatabaseReference.child(uidCourse).removeValue().addOnSuccessListener {
            deleteCourseStatus.value = "Succes"
        }.addOnFailureListener {
            deleteCourseStatus.value = "Fail"
        }
    }
}