package com.aluma.laundry.data.api.machine

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MachineViewModel (
    private val storePreferences: StorePreferences,
): ViewModel() {
    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.Companion.HTTPS
            host = "0f9489584773.ngrok-free.app"
        }
    )

    private val collection = "Machine"

    private val _token = MutableStateFlow<String?>(null)
    private val _idUser = MutableStateFlow<String?>(null)
    private val _idStore = MutableStateFlow<String?>(null)
    private val _isLoggedIn = MutableStateFlow(false)

    private val _machine = MutableStateFlow<List<Machine>>(emptyList())
    val machine: StateFlow<List<Machine>> = _machine

    private val _machineFilter = MutableStateFlow<List<Machine>>(emptyList())
    val machineFilter: StateFlow<List<Machine>> = _machineFilter

    private val _selectedMachine = MutableStateFlow<Machine?>(null)
    val selectedMachine: StateFlow<Machine?> = _selectedMachine

    fun setSelectedMachine(machine: Machine?) {
        _selectedMachine.value = machine
    }

    init {
        viewModelScope.launch {
            storePreferences.userIdUser.collectLatest { _idUser.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userIdStore.collectLatest { _idStore.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userToken.collectLatest {
                _token.value = it
                val loggedIn = !it.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    client.login(it)
                    fetchMachine()
                }
            }
        }
    }

    fun fetchMachine() {
        viewModelScope.launch {
            try {
                val fetched = client.records.getList<Machine>(collection, page = 1, perPage = 100)
                _machine.value = fetched.items.reversed()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Fetch machine failed", e)
            }
        }
    }

    fun filterMachine(type: Int, size: Boolean, stepMachine: Int) {
        viewModelScope.launch {
            val filtered = _machine.value.filter { machine ->
                // Filter berdasarkan type + stepMachine jika type == 2
                val matchType = when (type) {
                    0 -> !machine.typeMachine
                    1 -> machine.typeMachine
                    2 -> when (stepMachine) {
                        0 -> !machine.typeMachine
                        1 -> machine.typeMachine
                        else -> true // jika stepMachine tidak valid, lolos semua
                    }
                    else -> false
                }

                val matchSize = machine.sizeMachine == size

                matchType && matchSize
            }

            _machineFilter.value = filtered
            Log.d("SseClient", "Filtered machines: ${_machineFilter.value}")
        }
    }

    fun patchMachine(id: String?, machine: Machine) {
        viewModelScope.launch {
            try {
                client.records.update<Machine>(
                    id = id!!,
                    sub = collection,
                    body = Json.encodeToString(machine)
                )
//                Log.d("MainViewModel", "Item updated: $updated")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Patch machine failed", e)
            }
        }
    }
}