package com.aluma.laundry.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "store_prefs")

class StorePreferences(private val context: Context) {

    companion object {
        val KEY_EMAIL = stringPreferencesKey("email")
        val KEY_PASSWORD = stringPreferencesKey("password")
        val KEY_TOKEN = stringPreferencesKey("token")
        val KEY_ID_USER = stringPreferencesKey("id_user")
        val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val KEY_ID_STORE = stringPreferencesKey("id_store")
    }

    // ✅ Simpan data login
    suspend fun saveLogin(
        email: String,
        password: String,
        token: String,
        idUser: String,
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL] = email
            prefs[KEY_PASSWORD] = password
            prefs[KEY_TOKEN] = token
            prefs[KEY_ID_USER] = idUser
            prefs[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun saveStore(
        idStore: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ID_STORE] = idStore
        }
    }

    // 🧹 Hapus semua data login
    suspend fun clearLogin() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // 🔁 Data sebagai Flow (observasi)
    val userEmail: Flow<String?> = context.dataStore.data.map { it[KEY_EMAIL] }
    val userPassword: Flow<String?> = context.dataStore.data.map { it[KEY_PASSWORD] }
    val userToken: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }
    val userIdUser: Flow<String?> = context.dataStore.data.map { it[KEY_ID_USER] }
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { it[KEY_IS_LOGGED_IN] ?: false }
    val userIdStore: Flow<String?> = context.dataStore.data.map { it[KEY_ID_STORE] }
}