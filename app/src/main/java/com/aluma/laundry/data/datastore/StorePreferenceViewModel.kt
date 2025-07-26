package com.aluma.laundry.data.datastore

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StorePreferenceViewModel(
    private val storePreferences: StorePreferences,
): ViewModel() {

    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.Companion.HTTPS
            host = "03d4a5b4817b.ngrok-free.app"
        }
    )
    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token
    private val _idUser = MutableStateFlow<String?>(null)
    val idUser: StateFlow<String?> = _idUser
    private val _idStore = MutableStateFlow<String?>(null)
    val idStore: StateFlow<String?> = _idStore
    private val _nameStore = MutableStateFlow<String?>(null)
    val nameStore: StateFlow<String?> = _nameStore
    private val _addressStore = MutableStateFlow<String?>(null)
    val addressStore: StateFlow<String?> = _addressStore
    private val _cityStore = MutableStateFlow<String?>(null)
    val cityStore: StateFlow<String?> = _cityStore

    private val _bluetoothName = MutableStateFlow<String?>(null)
    val bluetoothName: StateFlow<String?> = _bluetoothName

    private val _bluetoothAddress = MutableStateFlow<String?>(null)
    val bluetoothAddress: StateFlow<String?> = _bluetoothAddress

    private val _loading = MutableStateFlow<Boolean?>(true)
    val loading: StateFlow<Boolean?> = _loading

    fun setLoading(stat: Boolean){
        _loading.value = stat
    }

    private val _isLoggedIn = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            storePreferences.bluetoothName.collectLatest { _bluetoothName.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.bluetoothAddress.collectLatest { _bluetoothAddress.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userIdUser.collectLatest { _idUser.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userIdStore.collectLatest { _idStore.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userNameStore.collectLatest { _nameStore.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userCityStore.collectLatest { _cityStore.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userStreetStore.collectLatest { _addressStore.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userToken.collectLatest {
                _token.value = it
                val loggedIn = !it.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                if (loggedIn) {
                    client.login(it)
                }
            }
        }
    }

    fun clearData(){
        viewModelScope.launch {
            storePreferences.clearLogin()
        }
    }

    fun saveBluetooth(
        bluetoothName: String,
        bluetoothAddress: String
    ){
        viewModelScope.launch {
            storePreferences.saveBluetooth(bluetoothName = bluetoothName, bluetoothAddress = bluetoothAddress)
        }
    }
}