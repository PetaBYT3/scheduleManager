package com.schedule.rt.sync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassCourse

class ViewModelCourse: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("courses")

    var uidMajor: String? = null
    var uidLevel: String? = null
    var uidClasses: String? = null

    private val _dataCourse = MutableLiveData<List<DataClassCourse>>()
    val dataCourse: LiveData<List<DataClassCourse>> get() =  _dataCourse

    fun getCourse() {
        val ref = databaseReference.orderByChild("uidClasses").equalTo(uidClasses)
        ref.addValueEventListener(object : ValueEventListener {
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

    fun getCourseByUid(uidCourse : String): LiveData<DataClassCourse?> {
        val dataCourse = MutableLiveData<DataClassCourse?>()
        val ref = databaseReference.child(uidCourse)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val getCourse = snapshot.getValue(DataClassCourse::class.java)
                dataCourse.value = getCourse
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return dataCourse
    }

    fun addCourse(dataClassCourse: DataClassCourse): LiveData<String?> {
        val addCourseStatus = MutableLiveData<String?>()
        val uidCourse = databaseReference.push().key.toString()
        val nameCourse = dataClassCourse.nameCourse
        dataClassCourse.uidMajor = uidMajor
        dataClassCourse.uidLevel = uidLevel
        dataClassCourse.uidClasses = uidClasses
        dataClassCourse.uidCourse = uidCourse
        val ref = databaseReference.orderByChild("uidClasses").equalTo(uidClasses)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExist = false
                for (child in snapshot.children) {
                    val dataCourse = child.getValue(DataClassCourse::class.java)
                    if (dataCourse?.nameCourse == nameCourse && dataCourse?.uidCourse != uidCourse) {
                        isExist = true
                        break
                    }
                }

                if (isExist == true) {
                    addCourseStatus.value = "Exist"
                } else {
                    databaseReference.child(uidCourse).setValue(dataClassCourse).addOnSuccessListener {
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
        return addCourseStatus
    }

    fun editCourse(dataClassCourse: DataClassCourse): LiveData<String?> {
        val editCourseStatus = MutableLiveData<String?>()
        val uidCourse = dataClassCourse.uidCourse.toString()
        val nameCourse = dataClassCourse.nameCourse
        val uidLecturer = dataClassCourse.uidLecturer
        val ref = databaseReference.orderByChild("uidClasses").equalTo(uidClasses)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isExist = false
                for (child in snapshot.children) {
                    val dataCourse = child.getValue(DataClassCourse::class.java)
                    if (dataCourse?.nameCourse == nameCourse && dataCourse?.uidCourse != uidCourse) {
                        isExist = true
                        break
                    }
                }

                if (isExist == true) {
                    editCourseStatus.value = "Exist"
                } else {
                    val updateChild = mapOf(
                        "nameCourse" to nameCourse,
                        "sksCourse" to dataClassCourse.sksCourse,
                        "uidLecturer" to uidLecturer
                    )
                    databaseReference.child(uidCourse).updateChildren(updateChild).addOnSuccessListener {
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
        return editCourseStatus
    }

    fun deleteCourse(uidCourse : String): LiveData<String?> {
        val deleteCourseStatus = MutableLiveData<String?>()
        databaseReference.child(uidCourse).removeValue().addOnSuccessListener {
            deleteCourseStatus.value = "Succes"
        }.addOnFailureListener {
            deleteCourseStatus.value = "Fail"
        }
        return deleteCourseStatus
    }

    fun getCourseSizeByClasses(uidClasses: String): LiveData<Int> {
        val courseSize = MutableLiveData<Int>()
        val ref = databaseReference.orderByChild("uidClasses").equalTo(uidClasses)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val size = snapshot.childrenCount.toInt()
                courseSize.value = size
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return courseSize
    }
}