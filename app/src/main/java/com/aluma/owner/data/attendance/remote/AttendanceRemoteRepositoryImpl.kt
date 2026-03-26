package com.aluma.owner.data.attendance.remote

import android.util.Log
import com.aluma.owner.data.attendance.model.LaundryAttendance
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import kotlinx.serialization.json.Json

class AttendanceRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : AttendanceRemoteRepository {

    private val collection = "LaundryAttendance"

    override suspend fun fetchAttendanceByEmployee(employeeId: String, monthFilter: String?): List<LaundryAttendance> {
        return try {
            val filter = if (monthFilter == null) {
                Filter("employee=\"$employeeId\"")
            } else {
                Filter("employee=\"$employeeId\" && date ~ \"$monthFilter\"")
            }
            client.records.getList<LaundryAttendance>(
                collection,
                page = 1,
                perPage = 100,
                filterBy = filter
            ).items.reversed()
        } catch (e: Exception) {
            Log.e("AttendanceRepo", "❌ fetchAttendance failed", e)
            emptyList()
        }
    }
}
