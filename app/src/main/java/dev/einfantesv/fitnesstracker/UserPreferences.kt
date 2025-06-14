package dev.einfantesv.fitnesstracker

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {

    companion object {
        private const val DATASTORE_NAME = "user_prefs"
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")

        // Extension para obtener DataStore en un Context
        private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)
    }

    // Guardar el correo
    suspend fun saveUserEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL_KEY] = email
        }
    }

    // Leer el correo
    fun getUserEmail(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[USER_EMAIL_KEY]
        }
    }

    // Eliminar el correo (cerrar sesión)
    suspend fun clearUserEmail() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_EMAIL_KEY)
        }
    }
}