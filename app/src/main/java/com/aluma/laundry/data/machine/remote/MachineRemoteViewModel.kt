package com.aluma.laundry.data.machine.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.machine.model.MachineRemote
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MachineRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val machineRepository: MachineRemoteRepository,
    private val client: PocketbaseClient
) : ViewModel() {

    private val _machineRemote = MutableStateFlow<List<MachineRemote>>(emptyList())
    val machineRemote: StateFlow<List<MachineRemote>> = _machineRemote

    private val _selectedMachineRemote = MutableStateFlow<MachineRemote?>(null)
    val selectedMachineRemote: StateFlow<MachineRemote?> = _selectedMachineRemote


    fun setSelectedMachine(machineRemote: MachineRemote?) {
        _selectedMachineRemote.value = machineRemote
    }

    init {
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
                    client.login(token)
                }
            }
        }
    }

    fun fetchMachine(storeID: String) {
        viewModelScope.launch {
            try {
                val fetched = machineRepository.fetchRemoteMachines(storeID)
                _machineRemote.value = fetched
            } catch (e: Exception) {
                Log.e("MachineVM", "Fetch failed", e)
            }
        }
    }

    fun updateTimer(machineId: String, newTimer: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                machineRepository.updateMachineTimer(machineId, newTimer)
                fetchMachine(_selectedMachineRemote.value?.store ?: "")
                onResult(true)
            } catch (e: Exception) {
                Log.e("MachineVM", "❌ Update timer failed", e)
                onResult(false)
            }
        }
    }
}