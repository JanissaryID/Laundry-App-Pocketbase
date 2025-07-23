package com.aluma.laundry.data.user.remote

import com.aluma.laundry.data.datastore.StorePreferences
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserRemoteViewModel(
    private val storePreferences: StorePreferences,
    private val userRepository: UserRemoteRepository
) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password

    private val _token = MutableStateFlow<String?>(null)

    private val _idUser = MutableStateFlow<String?>(null)

    val isLoading = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)
    private val _isLoggedIn = MutableStateFlow(false)
    private val _showSuccessLogin = MutableStateFlow(false)

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
            }
        }
    }

    fun login(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null

            if (_email.value.isBlank() || _password.value.isBlank()) {
                val msg = "Email atau password tidak boleh kosong"
                errorMessage.value = msg
                onError(msg)
                isLoading.value = false
                return@launch
            }

            try {
                val result = userRepository.login(_email.value, _password.value)

                _token.value = result.token
                _idUser.value = result.userId
                _isLoggedIn.value = true
                _showSuccessLogin.value = true

                storePreferences.saveLogin(_email.value, _password.value, result.token, result.userId)
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