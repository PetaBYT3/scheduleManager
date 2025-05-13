package com.schedule.rt.sync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelData: ViewModel() {

    val btnAddSchedule = MutableLiveData<Boolean>()
    val btnEditSchedule = MutableLiveData<Boolean>()

    private val _uidLecturer = MutableLiveData<String?>()
    val uidLecturer: LiveData<String?> get() = _uidLecturer

    fun sendUidLecturer(uidLecturer: String?) {
        _uidLecturer.value = uidLecturer
    }

    private val _uidMajor = MutableLiveData<String>()
    val uidMajor: LiveData<String> get() = _uidMajor

    fun sendUidMajor(uidMajor: String) {
        _uidMajor.value = uidMajor
    }

    private val _uidLevel = MutableLiveData<String>()
    val uidLevel: LiveData<String> get() = _uidLevel

    fun sendUidLevel(uidLevel: String) {
        _uidLevel.value = uidLevel
    }

    private val _uidClasses = MutableLiveData<String>()
    val uidClasses: LiveData<String> get() = _uidClasses

    fun sendUidClass(uidClasses: String) {
        _uidClasses.value = uidClasses
    }

    private val _uidCourse = MutableLiveData<String>()
    val uidCourse: LiveData<String> get() = _uidCourse

    fun sendUidCourse(uidCourse: String) {
        _uidCourse.value = uidCourse
    }

    private val _nameUser = MutableLiveData<String>()
    val nameUser: LiveData<String> get() = _nameUser

    fun sendNameUser(nameUser: String) {
        _nameUser.value = nameUser
    }
}