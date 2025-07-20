package com.aluma.laundry.store

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoreViewModel (
    private val storePreferences: StorePreferences,
): ViewModel() {
    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.Companion.HTTPS
            host = "03d4a5b4817b.ngrok-free.app"
        }
    )

    private val collection = "Store"

    private val _store = MutableStateFlow<List<Store>>(emptyList())
    val store: StateFlow<List<Store>> = _store

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    init {
        viewModelScope.launch {
            storePreferences.userToken.collectLatest {
                _token.value = it
                val loggedIn = !it.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    client.login(it)
                    fetchStore()
                }
            }
        }
    }

    fun fetchStore() {
        viewModelScope.launch {
            try {
                val fetched = client.records.getList<Store>(collection, page = 1, perPage = 500)
                _store.value = fetched.items.reversed()
            } catch (e: Exception) {
                Log.e("MainViewModel", "Fetch store failed", e)
            }
        }
    }
}