package com.aluma.laundry.data.machine.local

import android.util.Log
import com.aluma.laundry.data.machine.model.MachineLocal
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MachineLocalViewModel(private val repo: MachineLocalRepository) : ViewModel() {

    private val _machineFilter = MutableStateFlow<List<MachineLocal>>(emptyList())
    val machineFilter: StateFlow<List<MachineLocal>> = _machineFilter

    private val _machineFilterByStore = MutableStateFlow<List<MachineLocal>>(emptyList())
    val machineFilterByStore: StateFlow<List<MachineLocal>> = _machineFilterByStore

    private val _selectedMachine = MutableStateFlow<MachineLocal?>(null)
    val selectedMachine: StateFlow<MachineLocal?> = _selectedMachine

    fun setSelectMachine(machine: MachineLocal){
        _selectedMachine.value = machine
    }

    private var pendingStoreId: String? = null

    val machines: StateFlow<List<MachineLocal>> = repo.machineLocal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        observeMachines()
    }

    private fun observeMachines() {
        viewModelScope.launch {
            machines.collectLatest { list ->
                Log.d("SseClient", "Observed machine list: $list")
                pendingStoreId?.let { storeID ->
                    val filtered = list.filter { it.store == storeID }
                    _machineFilterByStore.value = filtered
                    Log.d("SseClient", "Auto-filtered machines by store '$storeID': $filtered")
                }
            }
        }
    }

    fun filterMachineByStore(storeID: String) {
        pendingStoreId = storeID

        val currentMachines = machines.value.orEmpty()
        if (currentMachines.isNotEmpty()) {
            val filtered = currentMachines.filter { it.store == storeID }
            _machineFilterByStore.value = filtered
            Log.d("SseClient", "Filtered machines by store '$storeID': $filtered")
        } else {
            Log.d("SseClient", "Machine list is empty, waiting for observer to filter.")
        }
    }

    fun updateMachine(machineLocal: MachineLocal) = viewModelScope.launch {
        repo.updateMachine(machineLocal)
    }

    fun deleteAllMachines() {
        viewModelScope.launch {
            repo.deleteAllMachines()
        }
    }
}