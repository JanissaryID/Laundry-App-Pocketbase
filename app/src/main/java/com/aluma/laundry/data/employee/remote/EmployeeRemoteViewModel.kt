package com.aluma.laundry.data.employee.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.employee.model.EmployeeRemote
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.aluma.laundry.data.realtime.remote.RealtimeViewModel

class EmployeeRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val client: PocketbaseClient,
    private val employeeRepository: EmployeeRemoteRepository,
    private val realtimeViewModel: RealtimeViewModel
) : ViewModel() {

    private val _token = MutableStateFlow<String?>(null)
    private val _isLoggedIn = MutableStateFlow(false)
    private val _userID = MutableStateFlow<String?>(null)
    private val _storeID = MutableStateFlow<String?>(null)

    private val _employees = MutableStateFlow<List<EmployeeRemote>>(emptyList())
    val employees: StateFlow<List<EmployeeRemote>> = _employees

    private val _selectedEmployee = MutableStateFlow<EmployeeRemote?>(null)
    val selectedEmployee: StateFlow<EmployeeRemote?> = _selectedEmployee

    private val _employeeId = MutableStateFlow<String?>(null)
    val employeeId: StateFlow<String?> = _employeeId

    private val _employeeName = MutableStateFlow<String?>(null)
    val employeeName: StateFlow<String?> = _employeeName

    init {
        viewModelScope.launch {
            storePreferences.userIdUser.collectLatest { _userID.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userIdStore.collectLatest { _storeID.value = it }
        }
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                _token.value = token
                val loggedIn = !token.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    client.login(token)
                }
            }
        }
        viewModelScope.launch {
            storePreferences.employeeId.collectLatest { _employeeId.value = it }
        }
        viewModelScope.launch {
            storePreferences.employeeName.collectLatest { _employeeName.value = it }
        }
        viewModelScope.launch {
            realtimeViewModel.realtimeEvent.collectLatest { collection ->
                if (collection == "LaundryEmployee") {
                    fetchEmployees()
                }
            }
        }
    }

    fun fetchEmployees() {
        viewModelScope.launch {
            try {
                val fetched = employeeRepository.fetchEmployees(
                    userID = _userID.value.orEmpty(),
                    storeID = _storeID.value
                )
                _employees.value = fetched
                Log.d("EmployeeVM", "✅ Fetched ${fetched.size} employees")
            } catch (e: Exception) {
                Log.e("EmployeeVM", "❌ Fetch Employees failed", e)
            }
        }
    }

    fun selectEmployee(employee: EmployeeRemote) {
        _selectedEmployee.value = employee
        _employeeId.value = employee.id
        _employeeName.value = employee.name
        viewModelScope.launch {
            storePreferences.saveEmployee(
                employeeId = employee.id.orEmpty(),
                employeeName = employee.name.orEmpty()
            )
        }
    }

    fun clearEmployee() {
        _selectedEmployee.value = null
        _employeeId.value = null
        _employeeName.value = null
        viewModelScope.launch {
            storePreferences.clearEmployee()
        }
    }
}
