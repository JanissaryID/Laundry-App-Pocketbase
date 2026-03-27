package com.aluma.laundry.data.order.remote

import android.content.Context
import com.aluma.laundry.data.datastore.StorePreferences
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.util.Log
import com.aluma.laundry.data.realtime.remote.RealtimeViewModel

class OrderRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val orderRepository: OrderRemoteRepository,
    private val client: PocketbaseClient,
    private val appContext: Context,
    private val realtimeViewModel: RealtimeViewModel
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
        viewModelScope.launch {
            realtimeViewModel.realtimeEvent.collectLatest { collection ->
                if (collection == "LaundryOrder") {
                    Log.d("OrderRemoteViewModel", "SSE Event: LaundryOrder changed.")
                }
            }
        }
    }

//    fun syncNow() {
//        enqueueSyncNow(appContext)
//    }

//    fun createOrder(order: OrderRemote) {
//        viewModelScope.launch {
//            if (_isLoggedIn.value) {
//                orderRepository.createOrder(order)
//            } else {
//                Log.w("OrderViewModel", "🔒 Cannot create order, not logged in")
//            }
//        }
//    }
}