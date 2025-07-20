package com.aluma.laundry.user

import com.aluma.laundry.data.datastore.StorePreferences
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord
import io.ktor.http.URLProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserViewModel(
    private val storePreferences: StorePreferences,
): ViewModel() {

    private val client = PocketbaseClient(
        baseUrl = {
            protocol = URLProtocol.Companion.HTTPS
            host = "03d4a5b4817b.ngrok-free.app"
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

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    init {
        viewModelScope.launch {
            storePreferences.userEmail.collectLatest { _email.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userPassword.collectLatest { _password.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userIdUser.collectLatest { _idUser.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userToken.collectLatest {
                _token.value = it
                val loggedIn = !it.isNullOrEmpty()
                _isLoggedIn.value = loggedIn
                _showSuccessLogin.value = loggedIn
                if (loggedIn) {
                    client.login(it)

                }
            }
        }
    }
    fun login(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            // Validasi form
            if (_email.value.isBlank() || _password.value.isBlank()) {
                val msg = "Email atau password tidak boleh kosong"
                errorMessage.value = msg
                onError(msg)
                isLoading.value = false
                return@launch
            }

            try {
                println("login")
                val loginResult = client.records.authWithPassword<AuthRecord>(
                    collection = "users",
                    email = _email.value,
                    password = _password.value
                )
                println("Login = $loginResult")

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
                val msg = "Login gagal: ${e.localizedMessage ?: "Terjadi kesalahan"}"
                errorMessage.value = msg
                onError(msg)
            } finally {
                isLoading.value = false
            }
        }
    }
}