package com.schedule.rt.sync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassLecturer
import com.schedule.rt.sync.dataclass.DataClassUser

class ViewModelUser(application: Application) : AndroidViewModel(application) {

    init {
        getUser()
    }

    private val _dataUser = MutableLiveData<DataClassUser?>()
    val dataUser : LiveData<DataClassUser?> get() = _dataUser

    fun getUser() {
        val uidUser = FirebaseAuth.getInstance().currentUser?.uid
        if (uidUser != null) {
            val userDataBaseReference = FirebaseDatabase.getInstance().getReference("users").child(uidUser.toString())
            userDataBaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val dataUser = snapshot.getValue(DataClassUser::class.java)
                        _dataUser.value = dataUser

                        // Get Lecturer
                        val uidLecturer = dataUser?.uidLecturer
                        if (uidLecturer != null) {
                            getLecturer(uidLecturer)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }

    private val _dataLecturer = MutableLiveData<DataClassLecturer?>()
    val dataLecturer : LiveData<DataClassLecturer?> get() = _dataLecturer

    fun getLecturer(uidLecturer : String) {
        val lecturerDatabaseReference = FirebaseDatabase.getInstance().getReference("lecturers").child(uidLecturer)
        lecturerDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val dataLecturer = snapshot.getValue(DataClassLecturer::class.java)
                    _dataLecturer.value = dataLecturer
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}