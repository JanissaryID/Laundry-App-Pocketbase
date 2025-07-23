package com.aluma.laundry.data.room.machine

import android.util.Log
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MachineRoomViewModel(private val repo: MachineRepository) : ViewModel() {

    private val _machineFilter = MutableStateFlow<List<MachineRoom>>(emptyList())
    val machineFilter: StateFlow<List<MachineRoom>> = _machineFilter

    private val _selectedMachine = MutableStateFlow<MachineRoom?>(null)
    val selectedMachine: StateFlow<MachineRoom?> = _selectedMachine

    val machines: StateFlow<List<MachineRoom>> = repo.machineRoom
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun filterMachine(type: Int, size: Boolean, stepMachine: Int) {
        viewModelScope.launch {
            Log.d("SseClient", "Filtered machines: ${machines}")
            val filtered = machines.value.filter { machine ->
                val typeMachine = machine.typeMachine
                val sizeMachine = machine.sizeMachine

                val matchType = when (type) {
                    0 -> !typeMachine
                    1 -> typeMachine
                    2 -> when (stepMachine) {
                        0 -> !typeMachine
                        1 -> typeMachine
                        else -> true
                    }
                    else -> false
                }

                val matchSize = sizeMachine == size

                matchType && matchSize
            }

            _machineFilter.value = filtered
            Log.d("SseClient", "Filtered machines: ${_machineFilter.value}")
        }
    }

    fun addMachine(machineRoom: MachineRoom) = viewModelScope.launch {
        repo.addMachine(machineRoom)
    }

    fun deleteMachine(machineRoom: MachineRoom) = viewModelScope.launch {
        repo.deleteMachine(machineRoom)
    }

    fun updateMachine(machineRoom: MachineRoom) = viewModelScope.launch {
        repo.updateMachine(machineRoom)
    }

    suspend fun getMachineById(id: String): MachineRoom? {
        return repo.getMachineById(id)
    }

}