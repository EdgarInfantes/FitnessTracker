package dev.einfantesv.fitnesstracker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserSessionViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userUid = MutableStateFlow<String?>(auth.currentUser?.uid)
    val userUid: StateFlow<String?> = _userUid

    private val _userData = MutableStateFlow<UserModel?>(null)
    val userData: StateFlow<UserModel?> = _userData

    val profileImageUrl: StateFlow<String?> = userData
        .map { it?.profileImageUrl }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        observeUidChanges()
    }

    private fun observeUidChanges() {
        viewModelScope.launch {
            userUid.collect { uid ->
                if (uid != null) {
                    loadUserData(uid)
                } else {
                    _userData.value = null
                }
            }
        }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUserUid(): String? = auth.currentUser?.uid

    fun refreshUserData(uid: String? = getCurrentUserUid()) {
        val safeUid = uid ?: return

        firestore.collection("User").document(safeUid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    _userData.value = snapshot.toObject(UserModel::class.java)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserSessionViewModel", "Error al refrescar datos del usuario", exception)
            }
    }

    fun signOut() {
        auth.signOut()
        _userUid.value = null
        _userData.value = null
    }

    fun loadUserData(uid: String? = auth.currentUser?.uid) {
        uid?.let {
            firestore.collection("User").document(it)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(UserModel::class.java)
                        println("Usuario cargado: $user")  // 👈 verifica en logcat
                        _userData.value = user
                    } else {
                        println("Documento no existe.")
                    }
                }
                .addOnFailureListener {
                    println("Error al cargar datos: ${it.message}")
                    _userData.value = null
                }
        }
    }

}

data class UserModel(
    val uid: String = "",
    val firstname: String = "",
    val lastname: String = "",
    val email: String = "",
    val UserFriendCode: Int = 0,
    val RegisterDate: Timestamp = Timestamp.now(),
    val privacy: Boolean = false,
    val profileImageUrl: String = ""
)