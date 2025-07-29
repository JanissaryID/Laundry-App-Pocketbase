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

class OrderRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val orderRepository: OrderRemoteRepository,
    private val client: PocketbaseClient,
) : ViewModel() {

    private val _orderRemote = MutableStateFlow<List<OrderRemote>>(emptyList())
    val orderRemote: StateFlow<List<OrderRemote>> = _orderRemote

    init {
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
                    client.login(token)
                }
            }
        }
    }

    fun fetchOrders(storeID: String) {
        viewModelScope.launch {
            try {
                val fetched = orderRepository.fetchOrder(storeID = storeID)
                _orderRemote.value = fetched
            } catch (e: Exception) {
                Log.e("ServiceViewModel", "❌ Fetch Services failed", e)
            }
        }
    }
}