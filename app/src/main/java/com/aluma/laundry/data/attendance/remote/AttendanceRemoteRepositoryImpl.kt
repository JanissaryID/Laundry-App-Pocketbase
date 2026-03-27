package com.aluma.laundry.data.attendance.remote

import android.util.Log
import com.aluma.laundry.data.attendance.model.AttendanceRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import io.github.agrevster.pocketbaseKotlin.models.Record
import kotlinx.serialization.json.Json

class AttendanceRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : AttendanceRemoteRepository {

    private val collection = "LaundryAttendance"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun checkIn(employeeId: String, date: String, time: String, storeId: String?): AttendanceRemote {
        val attendance = AttendanceRemote(
            employee = employeeId,
            date = date,
            cekIn = time,
            status = 1,
            store = storeId
        )
        val result = client.records.create<AttendanceRemote>(
            collection,
            json.encodeToString(attendance)
        )
        Log.d("AttendanceRepo", "✅ Check-in created at PB: ${result.id}")
        return result
    }

    override suspend fun checkOut(attendanceId: String, time: String) {
        val updateBody = """{"cekOut":"$time","status":2}"""
        client.records.update<Record>(
            collection,
            attendanceId,
            updateBody
        )
        Log.d("AttendanceRepo", "✅ Check-out updated: $attendanceId")
    }

    override suspend fun getTodayAttendance(employeeId: String, date: String): AttendanceRemote? {
        return try {
            val result = client.records.getList<AttendanceRemote>(
                collection,
                page = 1,
                perPage = 1,
                filterBy = Filter("employee=\"${employeeId}\" && date=\"${date}\"")
            )
            result.items.firstOrNull()
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "❌ Get today attendance failed", e)
            null
        }
    }
}
