package com.aluma.laundry.data.api.machine

import android.util.Log
import com.aluma.laundry.data.api.store.Store
import com.aluma.laundry.data.datastore.StorePreferences
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

    fun filterMachine(type: Int, size: Boolean) {
        viewModelScope.launch {
            val filtered = _machine.value.filter { machine ->
                val matchType = when (type) {
                    0 -> !machine.typeMachine
                    1 -> machine.typeMachine
                    2 -> !machine.typeMachine
                    else -> false // 2 = semua type
                }
                val matchSize = machine.sizeMachine == size
                matchType && matchSize
            }
            _machineFilter.value = filtered
        }
    }
}