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

    private val _employeeId = MutableStateFlow<String?>(null)
    val employeeId: StateFlow<String?> = _employeeId

    private val _employeeName = MutableStateFlow<String?>(null)
    val employeeName: StateFlow<String?> = _employeeName

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
            storePreferences.employeeId.collectLatest { _employeeId.value = it }
        }
        viewModelScope.launch {
            storePreferences.employeeName.collectLatest { _employeeName.value = it }
        }
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { tokenStr ->
                if (!tokenStr.isNullOrEmpty() && isTokenExpired(tokenStr)) {
                    android.util.Log.w("StorePreference", "Token expired! Clearing data...")
                    clearData()
                    return@collectLatest
                }

                _token.value = tokenStr
                val loggedIn = !tokenStr.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                if (loggedIn && tokenStr != null) {
                    client.login(tokenStr)
                }
            }
        }
    }

    private fun isTokenExpired(token: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size != 3) return true
            val payload = parts[1]
            val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            val expOpt = org.json.JSONObject(decodedString).optLong("exp")
            if (expOpt > 0) {
                val currentTimeSeconds = System.currentTimeMillis() / 1000
                // 5 minutes buffer
                return currentTimeSeconds >= (expOpt - 300)
            }
        } catch (e: Exception) {
            return true
        }
        return false

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