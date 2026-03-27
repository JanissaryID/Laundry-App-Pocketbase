package com.aluma.laundry.data.attendance.remote

import android.util.Log
import com.aluma.laundry.data.attendance.model.AttendanceRemote
import com.aluma.laundry.data.datastore.StorePreferences
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AttendanceRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val client: PocketbaseClient,
    private val attendanceRepository: AttendanceRemoteRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    private val _storeID = MutableStateFlow<String?>(null)

    private val _isCheckedIn = MutableStateFlow(false)
    val isCheckedIn: StateFlow<Boolean> = _isCheckedIn

    private val _todayAttendance = MutableStateFlow<AttendanceRemote?>(null)
    val todayAttendance: StateFlow<AttendanceRemote?> = _todayAttendance

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                val loggedIn = !token.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    client.login(token)
                }
            }
        }
        viewModelScope.launch {
            storePreferences.userIdStore.collectLatest { _storeID.value = it }
        }
    }

    fun fetchTodayAttendance(employeeId: String) {
        if (employeeId.isEmpty()) return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val attendance = attendanceRepository.getTodayAttendance(employeeId, today)
                _todayAttendance.value = attendance
                _isCheckedIn.value = attendance != null && attendance.cekOut.isNullOrEmpty()
                Log.d("AttendanceVM", "✅ Today attendance: $attendance, checkedIn: ${_isCheckedIn.value}")
            } catch (e: Exception) {
                Log.e("AttendanceVM", "❌ Fetch today attendance failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkIn(
        employeeId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (employeeId.isEmpty()) return
        if (_todayAttendance.value != null) {
            Log.w("AttendanceVM", "⚠️ Attendance already exists for today, skipping check-in")
            return
        }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                val attendance = attendanceRepository.checkIn(
                    employeeId = employeeId,
                    date = today,
                    time = time,
                    storeId = _storeID.value
                )
                _todayAttendance.value = attendance
                _isCheckedIn.value = true
                Log.d("AttendanceVM", "✅ Checked in: $attendance")
                onSuccess()
            } catch (e: Exception) {
                Log.e("AttendanceVM", "❌ Check-in failed", e)
                onError(e.message ?: "Check-in failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkOut(
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val attendance = _todayAttendance.value ?: return
        val attendanceId = attendance.id ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                attendanceRepository.checkOut(attendanceId, time)
                _todayAttendance.value = attendance.copy(cekOut = time, status = 2)
                _isCheckedIn.value = false
                Log.d("AttendanceVM", "✅ Checked out")
                onSuccess()
            } catch (e: Exception) {
                Log.e("AttendanceVM", "❌ Check-out failed", e)
                onError(e.message ?: "Check-out failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetAttendance() {
        _todayAttendance.value = null
        _isCheckedIn.value = false
    }
}
