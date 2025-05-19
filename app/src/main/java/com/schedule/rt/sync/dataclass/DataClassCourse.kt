package com.schedule.rt.sync.dataclass

data class DataClassCourse(
    var nameCourse: String? = null,
    var sksCourse: String? = null,

    var uidLecturer: String? = null,

    var uidMajor: String? = null,
    var uidLevel: String? = null,
    var uidClasses: String? = null,
    var uidCourse: String? = null,

    var uidBuilding: String? = null,
    var uidRoom: String? = null,
    var day : String? = null,

    var startTime: String? = null,
    var endTime: String? = null,
)
