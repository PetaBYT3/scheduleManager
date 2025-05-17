package com.schedule.rt.sync.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.schedule.rt.sync.dataclass.DataClassLecturer
import com.schedule.rt.sync.dataclass.DataClassUser

class ViewModelUser(application: Application) : AndroidViewModel(application) {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseReference = FirebaseDatabase.getInstance().getReference("users")

    fun getUser(): LiveData<DataClassUser?> {
        val dataUser = MutableLiveData<DataClassUser?>()
        val currentUser = firebaseAuth.currentUser?.uid
        if (currentUser != null) {
            val userDataBaseReference = FirebaseDatabase.getInstance().getReference("users").child(currentUser.toString())
            userDataBaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val getUser = snapshot.getValue(DataClassUser::class.java)
                        dataUser.value = getUser
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
        return dataUser
    }

    fun signIn(email : String?, password : String?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            result.value = "Empty"
        } else {
            firebaseAuth.signInWithEmailAndPassword(email.toString(), password.toString()).addOnCompleteListener {
                if (it.isSuccessful) {
                    result.value = "Success"
                } else {
                    result.value = "Fail"
                }
            }.addOnFailureListener {
                when (it) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        result.value = "Wrong"
                    }
                    is FirebaseAuthInvalidUserException -> {
                        result.value = "Not Exist"
                    }
                    else -> {
                        result.value = "Error"
                    }
                }
            }
        }
        return result
    }

    fun signUp(email : String?, password : String?, retypePassword : String?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        if (email.isNullOrEmpty() || password.isNullOrEmpty() || retypePassword.isNullOrEmpty()) {
            result.value = "Empty"
        } else {
            if (password != retypePassword) {
                result.value = "Password Not Match"
            } else {
                firebaseAuth.createUserWithEmailAndPassword(email.toString(), password.toString()).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val currentUser = it.result.user?.uid
                        if (currentUser != null) {
                            val updateChildren = mapOf(
                                "uidUser" to currentUser,
                            )
                            databaseReference.child(currentUser).updateChildren(updateChildren).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    result.value = "Success"
                                } else {
                                    result.value = "Fail"
                                }
                            }.addOnFailureListener {
                                result.value = "Error"
                            }
                        }
                    } else {
                        result.value = "Error"
                    }
                }.addOnFailureListener {
                    when (it) {
                        is FirebaseAuthWeakPasswordException -> {
                            result.value = "Password Short"
                        }

                        is FirebaseAuthInvalidCredentialsException -> {
                            result.value = "Invalid Email"
                        }

                        is FirebaseAuthUserCollisionException -> {
                            result.value = "Used Email"
                        }

                        else -> {
                            result.value = "Error"
                        }
                    }
                }
            }
        }
        return result
    }

    fun addUserFromGoogle(): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val currentUser = firebaseAuth.currentUser?.uid
        if (currentUser != null) {
            val updateChildren = mapOf(
                "uidUser" to currentUser,
            )
            databaseReference.child(currentUser).updateChildren(updateChildren).addOnCompleteListener {
                if (it.isSuccessful) {
                    result.value = "Success"
                } else {
                    result.value = "Fail"
                }
            }
        }
        return result
    }

    fun addUserData(dataUser: DataClassUser?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val currentUser = firebaseAuth.currentUser?.uid
        dataUser?.uidUser = currentUser
        val ref = databaseReference.child(currentUser.toString())
        ref.setValue(dataUser).addOnCompleteListener {
            if (it.isSuccessful) {
                result.value = "Success"
            } else {
                result.value = "Fail"
            }
        }.addOnFailureListener {
            result.value = "Error"
        }

        return result
    }

    fun getLecturerByNik(nik: String?): Pair<LiveData<String?>, LiveData<String?>> {
        val result = MutableLiveData<String?>()
        val uidLecturer = MutableLiveData<String?>()
        if (nik.isNullOrEmpty()) {
            result.value = "Empty"
        } else {
            val lecturerRef = FirebaseDatabase.getInstance().getReference("lecturers").orderByChild("nikLecturer").equalTo(nik)
            lecturerRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val dataLecturer = MutableLiveData<DataClassLecturer?>()
                        for (child in snapshot.children) {
                            val getData = child.getValue(DataClassLecturer::class.java)
                            dataLecturer.value = getData
                        }
                        uidLecturer.value = dataLecturer.value?.uidLecturer
                        result.value = "Success"
                    } else {
                        result.value = "Not Exist"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    result.value = "Error"
                }
            })
        }
        return Pair(result, uidLecturer)
    }

    fun addLecturerData(uidLecturer: String?): LiveData<String?> {
        val result = MutableLiveData<String?>()
        val currentUser = firebaseAuth.currentUser?.uid
        val dataUser = DataClassUser(
            uidLecturer = uidLecturer,
            uidUser = currentUser
        )
        databaseReference.child(currentUser.toString()).setValue(dataUser).addOnCompleteListener {
            if (it.isSuccessful) {
                result.value = "Success"
            } else {
                result.value = "Fail"
            }
        }
        return result
    }
}