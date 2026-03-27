package com.aluma.laundry.data.income.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.income.model.IncomeRemote
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.aluma.laundry.data.realtime.remote.RealtimeViewModel

class IncomeRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val incomeRemoteRepository: IncomeRemoteRepository,
    private val client: PocketbaseClient,
    private val realtimeViewModel: RealtimeViewModel
) : ViewModel() {
    private val _storeId = MutableStateFlow<String?>(null)
    val storeId: StateFlow<String?> = _storeId

    fun setStoreId(storeId: String?) {
        _storeId.value = storeId
    }

    init {
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
                    client.login(token)
                }
            }
        }
        viewModelScope.launch {
            realtimeViewModel.realtimeEvent.collectLatest { collection ->
                if (collection == "LaundryIncome") {
                    Log.d("IncomeRemoteViewModel", "SSE Event: LaundryIncome changed.")
                }
            }
        }
    }

    fun updateIncome(incomeId: String, total: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                incomeRemoteRepository.updateIncome(incomeId, total)
                onResult(true)
            } catch (e: Exception) {
                Log.e("MachineVM", "❌ Update Income failed", e)
                onResult(false)
            }
        }
    }

    fun fetchIncomeByDate(storeId: String, date: String, onResult: (List<IncomeRemote>) -> Unit) {
        viewModelScope.launch {
            try {
                val result = incomeRemoteRepository.getIncomeByDate(storeId, date)
                onResult(result)
            } catch (e: Exception) {
                Log.e("IncomeVM", "❌ Failed to fetch income by date", e)
                onResult(emptyList())
            }
        }
    }

}