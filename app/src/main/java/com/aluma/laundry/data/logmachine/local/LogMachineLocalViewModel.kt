package com.aluma.laundry.data.logmachine.local

import com.aluma.laundry.data.logmachine.model.LogMachineLocal
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.launch

class LogMachineLocalViewModel(
    private val repo: LogMachineLocalRepository,
) : ViewModel() {
    fun addLogMachine(logMachineLocal: LogMachineLocal) = viewModelScope.launch {
        repo.addLogMachine(logMachineLocal)
    }
}