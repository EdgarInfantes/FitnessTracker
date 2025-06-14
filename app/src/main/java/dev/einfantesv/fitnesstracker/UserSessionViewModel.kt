package dev.einfantesv.fitnesstracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.einfantesv.fitnesstracker.Screens.util.Constants.BASE_URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class UserSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreferences = UserPreferences(application)
    private val context = application.applicationContext

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    private val _profileImageUrl = MutableStateFlow("")
    val profileImageUrl: StateFlow<String> = _profileImageUrl

    // Guarda el correo tanto en memoria como en DataStore
    fun setUserEmail(email: String) {
        _userEmail.value = email
        viewModelScope.launch {
            userPreferences.saveUserEmail(email)
            fetchProfileImage(email)
        }
    }

    // Carga el email guardado en DataStore (usado al inicio)
    fun loadUserEmailFromDataStore() {
        viewModelScope.launch {
            userPreferences.getUserEmail().collect { email ->
                _userEmail.value = email
                email?.let { fetchProfileImage(it) }
            }
        }
    }

    // Cierra sesión y borra email
    fun clearUserEmail() {
        _userEmail.value = null
        _profileImageUrl.value = ""
        viewModelScope.launch {
            userPreferences.clearUserEmail()
        }
    }

    private suspend fun fetchProfileImage(email: String) {
        try {
            val url = URL("$BASE_URL/api/profile-pic/$email")
            withContext(Dispatchers.IO) {
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }

                    // Parseamos el JSON y extraemos la propiedad "profilePic"
                    val jsonObject = org.json.JSONObject(response)
                    val base64Image = jsonObject.optString("profilePic", "")

                    _profileImageUrl.value = base64Image.trim()
                } else {
                    _profileImageUrl.value = ""
                }
            }
        } catch (e: Exception) {
            _profileImageUrl.value = ""
        }
    }
}