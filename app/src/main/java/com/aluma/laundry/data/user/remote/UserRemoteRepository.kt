package com.aluma.laundry.data.user.remote

import com.aluma.laundry.data.user.model.LoginResult

interface UserRemoteRepository {
    suspend fun login(email: String, password: String): LoginResult
}