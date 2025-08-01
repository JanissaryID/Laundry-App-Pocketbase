package com.aluma.laundry.data.income.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.income.model.IncomeRemote
import com.aluma.laundry.data.store.remote.StoreRemoteRepository
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class IncomeRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val incomeRemoteRepository: IncomeRemoteRepository,
    private val storeRemoteRepository: StoreRemoteRepository,
    private val client: PocketbaseClient
) : ViewModel() {
    private val _storeId = MutableStateFlow<String?>(null)
    val storeId: StateFlow<String?> = _storeId

    private val _incomeRemote = MutableStateFlow<List<IncomeRemote>>(emptyList())
    val incomeRemote: StateFlow<List<IncomeRemote>> = _incomeRemote

    private val _incomeRemoteStore = MutableStateFlow<List<Triple<String, String, String>>>(emptyList())
    val incomeRemoteStore: StateFlow<List<Triple<String, String, String>>> = _incomeRemoteStore

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
    }

    fun fetchIncome(date: String) {
        viewModelScope.launch {
            try {
                val result = incomeRemoteRepository.fetchRemoteIncome(date)
                _incomeRemote.value = result
            } catch (e: Exception) {
                Log.e("IncomeVM", "❌ Failed to fetch income", e)
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

    fun incomeStore() {
        viewModelScope.launch {
            try {
                val stores = storeRemoteRepository.fetchStores()

                val incomeList = mutableListOf<Triple<String, String, String>>()

                for (store in stores) {
                    val storeId = store.id.orEmpty()
                    if (storeId.isBlank()) continue

                    val incomes = incomeRemoteRepository.getIncomeByStore(storeId)

                    for (income in incomes) {
                        val date = income.date.orEmpty()
                        val total = income.total?.toDoubleOrNull()?.toLong()?.toString() ?: "0"
                        incomeList.add(Triple(storeId, date, total))
                    }
                }

                _incomeRemoteStore.value = incomeList

            } catch (e: Exception) {
                Log.e("IncomeVM", "❌ Failed to fetch incomeStore", e)
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