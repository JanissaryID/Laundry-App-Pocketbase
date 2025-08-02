package com.aluma.owner.data.user.remote

import com.aluma.owner.data.user.model.LoginResult

interface UserRemoteRepository {
    suspend fun login(email: String, password: String): LoginResult
}