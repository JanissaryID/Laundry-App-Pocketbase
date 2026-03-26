package com.aluma.owner.data.attendance.remote

import com.aluma.owner.data.attendance.model.LaundryAttendance

interface AttendanceRemoteRepository {
    suspend fun fetchAttendanceByEmployee(employeeId: String, monthFilter: String? = null): List<LaundryAttendance>
}
