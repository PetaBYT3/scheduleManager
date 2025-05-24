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

class ViewModelCourse: ViewModel() {

    private val databaseReference = FirebaseDatabase.getInstance().getReference("courses")

    private val _uidCourse = MutableLiveData<String?>()
    val uidCourse: LiveData<String?> get() = _uidCourse

    fun sendCourseUid(uidCourse: String?) {
        _uidCourse.value = uidCourse
    }

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
    private val _uidClassesReference = MutableLiveData<String?>()
    val uidClassesReference: LiveData<String?> get() = _uidClassesReference

    fun uidClassesReference(uidClasses: String?) {
        _uidClassesReference.value = uidClasses
    }

    fun getCourse(): LiveData<List<DataClassCourse>> {
        val dataCourse = MutableLiveData<List<DataClassCourse>>()
        val ref = databaseReference.orderByChild("uidClasses").equalTo(uidClassesReference.value)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listCourse = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getCourse = dataSnapshot.getValue(DataClassCourse::class.java)
                    getCourse?.let { listCourse.add(it) }
                }
                dataCourse.value = listCourse
                Log.d("dataCourse", dataCourse.value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeCourse", "Error")
            }
        })
        return dataCourse
    }

    fun getCourseByLecturer(uidLecturer: String?) : LiveData<List<DataClassCourse>> {
        val dataCourse = MutableLiveData<List<DataClassCourse>>()
        val ref = databaseReference.orderByChild("uidLecturer").equalTo(uidLecturer)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val listCourse = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getCourse = dataSnapshot.getValue(DataClassCourse::class.java)
                    if (getCourse?.uidLecturer == uidLecturer) {
                        getCourse?.let { listCourse.add(it) }
                    }
                }
                dataCourse.value = listCourse
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeCourse", "Error")
            }
        })

        return dataCourse
    }

    fun getCourseByLecturerSchedule(uidLecturer: String?, day: String?) : LiveData<List<DataClassCourse>> {
        val dataCourse = MutableLiveData<List<DataClassCourse>>()
        val ref = databaseReference.orderByChild("uidLecturer").equalTo(uidLecturer)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listCourse = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getCourse = dataSnapshot.getValue(DataClassCourse::class.java)
                    val getDay = getCourse?.day
                    if (getDay == day) {
                        getCourse?.let { listCourse.add(it) }
                    }
                }
                dataCourse.value = listCourse
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeCourse", "Error")
            }
        })

        return dataCourse
    }

    fun getCourseByClassSchedule(uidClasses: String?, day: String?) : LiveData<List<DataClassCourse>> {
        val dataCourse = MutableLiveData<List<DataClassCourse>>()
        val ref = databaseReference.orderByChild("uidClasses").equalTo(uidClasses)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listCourse = mutableListOf<DataClassCourse>()
                for (dataSnapshot in snapshot.children) {
                    val getCourse = dataSnapshot.getValue(DataClassCourse::class.java)
                    val getDay = getCourse?.day
                    if (getDay == day) {
                        getCourse?.let { listCourse.add(it) }
                    }
                }
                dataCourse.value = listCourse
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeCourse", "Error")
            }
        })

        return  dataCourse
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
                Log.e("ViewModeCourse", "Error")
            }
        })
        return dataCourse
    }

    fun addCourse(dataClassCourse: DataClassCourse): LiveData<String?> {
        val addCourseStatus = MutableLiveData<String?>()
        val uidCourse = databaseReference.push().key.toString()
        val nameCourse = dataClassCourse.nameCourse
        dataClassCourse.uidMajor = uidMajorReference.value
        dataClassCourse.uidLevel = uidLevelReference.value
        dataClassCourse.uidClasses = uidClassesReference.value
        dataClassCourse.uidCourse = uidCourse
        val ref = databaseReference.orderByChild("uidClasses").equalTo(uidClassesReference.value)
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
                addCourseStatus.value = "Error"
            }
        })
        return addCourseStatus
    }

    fun editCourse(dataClassCourse: DataClassCourse): LiveData<String?> {
        val editCourseStatus = MutableLiveData<String?>()
        val uidCourse = dataClassCourse.uidCourse.toString()
        val nameCourse = dataClassCourse.nameCourse
        val uidLecturer = dataClassCourse.uidLecturer
        val ref = databaseReference.orderByChild("uidClasses").equalTo(uidClassesReference.value)
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
                    if (uidLecturer != null) {
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
                    } else {
                        val updateChild = mapOf(
                            "nameCourse" to nameCourse,
                            "sksCourse" to dataClassCourse.sksCourse
                        )
                        databaseReference.child(uidCourse).updateChildren(updateChild).addOnSuccessListener {
                            editCourseStatus.value = "Success"
                        }.addOnFailureListener {
                            editCourseStatus.value = "Fail"
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                editCourseStatus.value = "Error"
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
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val size = snapshot.childrenCount.toInt()
                courseSize.value = size
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeCourse", "Error")
            }
        })
        return courseSize
    }

    fun getCourseSizeByBuilding(uidBuilding: String?): LiveData<Int> {
        val courseSize = MutableLiveData<Int>()
        val ref = databaseReference.orderByChild("uidBuilding").equalTo(uidBuilding)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val size = snapshot.childrenCount.toInt()
                courseSize.value = size
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeCourse", "Error")
            }
        })
        return courseSize
    }

    fun getCourseSizeByRoom(uidRoom: String?): LiveData<Int> {
        val courseSize = MutableLiveData<Int>()
        val ref = databaseReference.orderByChild("uidRoom").equalTo(uidRoom)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val size = snapshot.childrenCount.toInt()
                courseSize.value = size
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeCourse", "Error")
            }
        })
        return courseSize
    }

    fun getCourseSizeByLevel(uidLevel: String?): LiveData<Int> {
        val courseSize = MutableLiveData<Int>()
        val ref = databaseReference.orderByChild("uidLevel").equalTo(uidLevel)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val size = snapshot.childrenCount.toInt()
                courseSize.value = size
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeCourse", "Error")
            }
        })
        return courseSize
    }

    fun getCourseSizeByMajor(uidMajor: String?): LiveData<Int> {
        val courseSize = MutableLiveData<Int>()
        val ref = databaseReference.orderByChild("uidMajor").equalTo(uidMajor)
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val size = snapshot.childrenCount.toInt()
                courseSize.value = size
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModeCourse", "Error")
            }
        })

        return courseSize
    }
}