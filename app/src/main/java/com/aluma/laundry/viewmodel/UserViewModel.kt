package com.aluma.laundry.viewmodel

import com.aluma.laundry.data.datastore.StorePreferences
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserViewModel(
    private val storePreferences: StorePreferences,
): ViewModel() {

    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.HTTPS
            host = "064a733d489b.ngrok-free.app"
        }
    )

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password
    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token

    private val _idUser = MutableStateFlow<String?>(null)
    val idUser: StateFlow<String?> = _idUser

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _showSuccessLogin = MutableStateFlow(false)
    val showSuccessLogin: StateFlow<Boolean> = _showSuccessLogin

    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                if (_email.value.isBlank() || _password.value.isBlank()) {
                    errorMessage.value = "Email atau password tidak boleh kosong"
                    onError(errorMessage.value!!)
                    return@launch
                }

                val loginResult = client.records.authWithPassword<AuthRecord>(
                    collection = "users",
                    email = _email.value,
                    password = _password.value
                )

                val token = loginResult.token
                val userId = loginResult.record.id.orEmpty()

                _token.value = token
                _idUser.value = userId
                _isLoggedIn.value = true
                _showSuccessLogin.value = true

                storePreferences.saveLogin(_email.value, _password.value, token, userId)
                client.login(token)
                onSuccess()
            } catch (e: Exception) {
                errorMessage.value = "Login gagal: ${e.localizedMessage ?: "Unknown error"}"
                onError(errorMessage.value!!)
            } finally {
                isLoading.value = false
            }
        }
    }
}