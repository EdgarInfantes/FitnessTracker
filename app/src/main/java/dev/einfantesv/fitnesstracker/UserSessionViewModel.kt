package dev.einfantesv.fitnesstracker

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class UserSessionViewModel : ViewModel() {
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    fun setUserEmail(email: String) {
        _userEmail.value = email
    }
}