package com.aluma.laundry.data.order.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.order.model.OrderRemote
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class OrderRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val orderRepository: OrderRemoteRepository,
    private val client: PocketbaseClient,
) : ViewModel() {

    private val _orderRemote = MutableStateFlow<List<OrderRemote>>(emptyList())
    val orderRemote: StateFlow<List<OrderRemote>> = _orderRemote

    private val _orderRemoteStore = MutableStateFlow<List<OrderRemote>>(emptyList())
    val orderRemoteStore: StateFlow<List<OrderRemote>> = _orderRemoteStore

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
    }

//    fun fetchOrdersStore(storeID: String) {
//        viewModelScope.launch {
//            try {
//                val fetched = orderRepository.fetchOrder(storeID = storeID)
//                _orderRemoteStore.value = fetched
//            } catch (e: Exception) {
//                Log.e("ServiceViewModel", "❌ Fetch Services failed", e)
//            }
//        }
//    }

    fun fetchOrdersByDate(date: LocalDate, storeID: String) {
        viewModelScope.launch {
            try {
                val fetched = orderRepository.fetchOrder(storeID = storeID, date = date)
                _orderRemoteStore.value = fetched
            } catch (e: Exception) {
                Log.e("OrderRemoteViewModel", "❌ Fetch Orders by Date failed", e)
            }
        }
    }
}