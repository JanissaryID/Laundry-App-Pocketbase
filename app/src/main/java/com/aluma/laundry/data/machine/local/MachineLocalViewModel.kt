package com.aluma.laundry.data.machine.local

import android.util.Log
import com.aluma.laundry.data.machine.model.MachineLocal
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MachineLocalViewModel(private val repo: MachineLocalRepository) : ViewModel() {

    private val _machineFilter = MutableStateFlow<List<MachineLocal>>(emptyList())
    val machineFilter: StateFlow<List<MachineLocal>> = _machineFilter

    private val _selectedMachine = MutableStateFlow<MachineLocal?>(null)
    val selectedMachine: StateFlow<MachineLocal?> = _selectedMachine

    fun setSelectMachine(machine: MachineLocal){
        _selectedMachine.value = machine
    }

    val machines: StateFlow<List<MachineLocal>> = repo.machineLocal
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

//    fun addMachine(machineLocal: MachineLocal) = viewModelScope.launch {
//        repo.addMachine(machineLocal)
//    }
//
//    fun deleteMachine(machineLocal: MachineLocal) = viewModelScope.launch {
//        repo.deleteMachine(machineLocal)
//    }

    fun updateMachine(machineLocal: MachineLocal) = viewModelScope.launch {
        repo.updateMachine(machineLocal)
    }

//    suspend fun getMachineById(id: String): MachineLocal? {
//        return repo.getMachineById(id)
//    }

    fun deleteAllMachines() {
        viewModelScope.launch {
            repo.deleteAllMachines()
        }
    }

}