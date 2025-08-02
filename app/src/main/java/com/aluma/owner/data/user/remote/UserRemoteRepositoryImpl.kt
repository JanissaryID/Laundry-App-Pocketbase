package com.aluma.owner.data.user.remote

import com.aluma.owner.data.user.model.LoginResult
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.models.AuthRecord

class UserRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : UserRemoteRepository {
    override suspend fun login(email: String, password: String): LoginResult {
        val loginResult = client.records.authWithPassword<AuthRecord>(
            collection = "users",
            email = email,
            password = password
        )

        val token = loginResult.token
        val userId = loginResult.record.id ?: throw IllegalStateException("User ID kosong")

        return LoginResult(token, userId)
    }
}