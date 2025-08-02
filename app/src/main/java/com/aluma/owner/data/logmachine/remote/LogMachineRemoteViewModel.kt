package com.aluma.owner.data.logmachine.remote

import android.util.Log
import com.aluma.owner.data.datastore.StorePreferences
import com.aluma.owner.data.logmachine.model.LogMachineRemote
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class LogMachineRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val logMachineRemoteRepository: LogMachineRemoteRepository,
    private val client: PocketbaseClient
) : ViewModel() {

    private val _logMachineRemote = MutableStateFlow<List<LogMachineRemote>>(emptyList())
    val logMachineRemote: StateFlow<List<LogMachineRemote>> = _logMachineRemote

    private val _storeName = MutableStateFlow<String?>(null)
    val storeName: StateFlow<String?> = _storeName

    private val _storeAddress = MutableStateFlow<String?>(null)
    val storeAddress: StateFlow<String?> = _storeAddress

    fun setStoreName(storeName: String?) {
        _storeName.value = storeName
    }

    fun setStoreAddress(storeAddress: String?) {
        _storeAddress.value = storeAddress
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

    fun fetchLogMachinesByDate(date: LocalDate, storeID: String) {
        viewModelScope.launch {
            try {
                val fetched = logMachineRemoteRepository.fetchLogMachine(storeID = storeID, date = date)
                _logMachineRemote.value = fetched
            } catch (e: Exception) {
                Log.e("OrderRemoteViewModel", "❌ Fetch Orders by Date failed", e)
            }
        }
    }
}