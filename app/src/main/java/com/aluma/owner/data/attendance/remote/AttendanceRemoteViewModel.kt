package com.aluma.owner.data.attendance.remote

import android.util.Log
import com.aluma.owner.data.attendance.model.LaundryAttendance
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AttendanceRemoteViewModel(
    private val attendanceRepository: AttendanceRemoteRepository
) : ViewModel() {

    private val _attendanceList = MutableStateFlow<List<LaundryAttendance>>(emptyList())
    val attendanceList: StateFlow<List<LaundryAttendance>> = _attendanceList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _totalPresent = MutableStateFlow(0)
    val totalPresent: StateFlow<Int> = _totalPresent

    // State Filter Bulan & Tahun
    private val calendar = java.util.Calendar.getInstance()
    private val _selectedMonth = MutableStateFlow(calendar.get(java.util.Calendar.MONTH) + 1) // 1-12
    val selectedMonth: StateFlow<Int> = _selectedMonth

    private val _selectedYear = MutableStateFlow(calendar.get(java.util.Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear

    fun updateFilter(month: Int, year: Int, employeeId: String) {
        _selectedMonth.value = month
        _selectedYear.value = year
        fetchAttendance(employeeId)
    }

    fun fetchAttendance(employeeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Format filter: yyyy-MM (Contoh: 2026-03)
                val monthStr = if (_selectedMonth.value < 10) "0${_selectedMonth.value}" else "${_selectedMonth.value}"
                val monthFilter = "${_selectedYear.value}-$monthStr"
                
                val result = attendanceRepository.fetchAttendanceByEmployee(employeeId, monthFilter)
                _attendanceList.value = result
                _totalPresent.value = result.count { it.status == 1 }
            } catch (e: Exception) {
                _errorMessage.value = "attendance_error_fetch"
                Log.e("AttendanceViewModel", "❌ fetchAttendance failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
