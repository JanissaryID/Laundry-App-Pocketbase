package com.aluma.laundry.data.order.remote

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.order.model.OrderRemote
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OrderRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val orderRepository: OrderRemoteRepository,
    private val client: PocketbaseClient
) : ViewModel() {

    private val _token = MutableStateFlow<String?>(null)
    private val _isLoggedIn = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                _token.value = token
                if (!token.isNullOrEmpty()) {
                    client.login(token)
                    _isLoggedIn.value = true
                }
            }
        }
    }

    fun createOrder(order: OrderRemote) {
        viewModelScope.launch {
            if (_isLoggedIn.value) {
                orderRepository.createOrder(order)
            } else {
                Log.w("OrderViewModel", "🔒 Cannot create order, not logged in")
            }
        }
    }
}