package com.aluma.laundry.data.machine.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.machine.local.MachineLocalRepository
import com.aluma.laundry.data.machine.model.MachineLocal
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
    private val machineLocalRepository: MachineLocalRepository,
    private val machineRepository: MachineRemoteRepository,
    private val client: PocketbaseClient
) : ViewModel() {

    private val _machineRemote = MutableStateFlow<List<MachineRemote>>(emptyList())
    val machineRemote: StateFlow<List<MachineRemote>> = _machineRemote

    private val _machineRemoteFilter = MutableStateFlow<List<MachineRemote>>(emptyList())
    val machineRemoteFilter: StateFlow<List<MachineRemote>> = _machineRemoteFilter

    private val _selectedMachineRemote = MutableStateFlow<MachineRemote?>(null)
    val selectedMachineRemote: StateFlow<MachineRemote?> = _selectedMachineRemote

    private val _isLoggedIn = MutableStateFlow(false)

    fun setSelectedMachine(machineRemote: MachineRemote?) {
        _selectedMachineRemote.value = machineRemote
    }

    init {
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
                    client.login(token)
                    _isLoggedIn.value = true
                    fetchMachine()
                }
            }
        }
    }

    private fun fetchMachine() {
        viewModelScope.launch {
            try {
                val fetched = machineRepository.fetchRemoteMachines()
                _machineRemote.value = fetched

                fetched.forEach { machine ->
                    val existing = machineLocalRepository.getMachineById(machine.id!!)

                    if (existing == null) {
                        val newMachine = MachineLocal(
                            id = machine.id!!,
                            numberMachine = machine.numberMachine,
                            typeMachine = machine.typeMachine,
                            sizeMachine = machine.sizeMachine,
                            user = machine.user,
                            store = machine.store,
                            bluetoothAddress = machine.bluetoothAddress,
                            timer = machine.timer,
                            inUse = false,
                            timeOn = null,
                            order = null,
                        )
                        machineLocalRepository.addMachine(newMachine)
                    } else {
                        val updated = existing.copy(
                            numberMachine = machine.numberMachine,
                            typeMachine = machine.typeMachine,
                            sizeMachine = machine.sizeMachine,
                            bluetoothAddress = machine.bluetoothAddress,
                            timer = machine.timer
                        )
                        machineLocalRepository.updateMachine(updated)
                    }
                }
            } catch (e: Exception) {
                Log.e("MachineVM", "Fetch failed", e)
            }
        }
    }
}