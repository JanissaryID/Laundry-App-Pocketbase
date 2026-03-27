package com.aluma.owner.data.employee.remote

import android.util.Log
import com.aluma.owner.data.datastore.StorePreferences
import com.aluma.owner.data.employee.model.LaundryEmployee
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import com.aluma.owner.data.attendance.remote.AttendanceRemoteRepository
import java.util.Calendar

class EmployeeRemoteViewModel(
    private val employeeRepository: EmployeeRemoteRepository,
    private val attendanceRepository: AttendanceRemoteRepository,
    private val client: PocketbaseClient,
    private val storePreferences: StorePreferences
) : ViewModel() {

    private val _employees = MutableStateFlow<List<LaundryEmployee>>(emptyList())
    val employees: StateFlow<List<LaundryEmployee>> = _employees

    private val _selectedEmployee = MutableStateFlow<LaundryEmployee?>(null)
    val selectedEmployee: StateFlow<LaundryEmployee?> = _selectedEmployee

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _attendanceCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val attendanceCounts: StateFlow<Map<String, Int>> = _attendanceCounts

    private val _ownerId = MutableStateFlow<String?>(null)

    fun setSelectedEmployee(employee: LaundryEmployee?) {
        _selectedEmployee.value = employee
    }

    init {
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
                    client.login(token)
                    // Fetch will be triggered by ownerId collection if not already
                }
            }
        }
        viewModelScope.launch {
            storePreferences.userIdUser.collectLatest { id ->
                _ownerId.value = id
                if (!id.isNullOrEmpty()) {
                    fetchEmployees(id)
                }
            }
        }
    }

    fun fetchEmployees(ownerId: String? = _ownerId.value) {
        if (ownerId == null) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val employeesList = employeeRepository.fetchEmployees(ownerId)
                _employees.value = employeesList
                
                // Fetch attendance counts for current month
                val calendar = Calendar.getInstance()
                val month = calendar.get(Calendar.MONTH) + 1
                val year = calendar.get(Calendar.YEAR)
                val monthStr = if (month < 10) "0$month" else "$month"
                val monthFilter = "$year-$monthStr"
                
                val counts = mutableMapOf<String, Int>()
                employeesList.forEach { emp ->
                    val attendance = attendanceRepository.fetchAttendanceByEmployee(emp.id.orEmpty(), monthFilter)
                    counts[emp.id.orEmpty()] = attendance.count { it.status == 1 }
                }
                _attendanceCounts.value = counts
            } catch (e: Exception) {
                _errorMessage.value = "Gagal memuat data karyawan"
                Log.e("EmployeeViewModel", "❌ fetchEmployees failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addEmployee(name: String, onResult: (Boolean) -> Unit) {
        val ownerId = _ownerId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val employee = LaundryEmployee(name = name, user = ownerId)
                employeeRepository.addEmployee(employee)
                fetchEmployees(ownerId)
                onResult(true)
            } catch (e: Exception) {
                Log.e("EmployeeViewModel", "❌ addEmployee failed", e)
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun editEmployee(employeeId: String, name: String, onResult: (Boolean) -> Unit) {
        val ownerId = _ownerId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val employee = LaundryEmployee(name = name, user = ownerId)
                employeeRepository.editEmployee(employee, employeeId)
                fetchEmployees(ownerId)
                onResult(true)
            } catch (e: Exception) {
                Log.e("EmployeeViewModel", "❌ editEmployee failed", e)
                fetchEmployees(ownerId)
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteEmployee(employeeId: String, onResult: (Boolean) -> Unit) {
        val ownerId = _ownerId.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                employeeRepository.deleteEmployee(employeeId)
                fetchEmployees(ownerId)
                onResult(true)
            } catch (e: Exception) {
                Log.e("EmployeeViewModel", "❌ deleteEmployee failed", e)
                fetchEmployees(ownerId)
                onResult(false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
