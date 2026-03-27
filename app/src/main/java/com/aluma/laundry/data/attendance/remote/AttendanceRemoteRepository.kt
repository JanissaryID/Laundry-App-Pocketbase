package com.aluma.laundry.data.attendance.remote

import com.aluma.laundry.data.attendance.model.AttendanceRemote

interface AttendanceRemoteRepository {
    suspend fun checkIn(employeeId: String, date: String, time: String, storeId: String? = null): AttendanceRemote
    suspend fun checkOut(attendanceId: String, time: String)
    suspend fun getTodayAttendance(employeeId: String, date: String): AttendanceRemote?
}
