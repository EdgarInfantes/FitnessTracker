package dev.einfantesv.fitnesstracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class UserSessionViewModel : ViewModel() {
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail

    fun setUserEmail(email: String) {
        _userEmail.value = email
    }

    // URL de imagen de perfil basada en el correo
    val profileImageUrl: StateFlow<String> = userEmail.map { email ->
        when (email.orEmpty()) {
            "dirtyyr2012@gmail.com" -> "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fstatic.wikia.nocookie.net%2Fficcion-sin-limites%2Fimages%2F5%2F50%2FJOEL.webp%2Frevision%2Flatest%2Fscale-to-width-down%2F1200%3Fcb%3D20220416041602%26path-prefix%3Des"
            "melva.66.2002@gmail.com" -> "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fstatic.wikia.nocookie.net%2Falfondohaysitio%2Fimages%2F8%2F8d%2FTeresa_(AFHS10).png%2Frevision%2Flatest%2Fscale-to-width-down%2F1200%3Fcb%3D20230512183136%26path-prefix%3Des"
            "alxmeza63@gmail.com" -> "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fi.pinimg.com%2Foriginals%2F76%2Fe7%2F48%2F76e7484504c6ae8efd1a4df5cdd282f5.jpg"
            "admin" -> "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fi.pinimg.com%2Foriginals%2F76%2Fe7%2F48%2F76e7484504c6ae8efd1a4df5cdd282f5.jpg"
            else -> ""
        }
    }.stateIn(viewModelScope, SharingStarted.Companion.Eagerly, "")
}