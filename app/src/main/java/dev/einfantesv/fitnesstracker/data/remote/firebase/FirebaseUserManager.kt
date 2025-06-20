package dev.einfantesv.fitnesstracker.data.remote.firebase

import android.annotation.SuppressLint
import com.google.firebase.auth.EmailAuthProvider

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUserManager {

    private val auth = FirebaseAuth.getInstance()

    @SuppressLint("StaticFieldLeak")

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Actualiza el nombre y/o apellido del usuario.
     */
    fun updateName(uid: String, name: String, lastname: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("User").document(uid)

            .update(mapOf("firstname" to name, "lastname" to lastname))

            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Actualiza el correo electrónico en FirebaseAuth y Firestore.
     */

    fun updateEmail(newEmail: String, currentPassword: String, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return onComplete(false)

        val currentEmail = user.email
        if (currentEmail.isNullOrBlank()) return onComplete(false)

        // Crear credencial para reautenticación
        val credential = EmailAuthProvider.getCredential(currentEmail, currentPassword)

        // Reautenticación necesaria antes de cambiar el email
        user.reauthenticate(credential)
            .addOnSuccessListener {
                // Si la reautenticación fue exitosa, actualiza el email
                user.updateEmail(newEmail)
                    .addOnSuccessListener {
                        firestore.collection("User").document(user.uid)
                            .update("email", newEmail)
                            .addOnSuccessListener { onComplete(true) }
                            .addOnFailureListener { onComplete(false) }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener {
                onComplete(false) // Falló la reautenticación
            }

    }

    /**
     * Actualiza la contraseña del usuario.
     */
    fun updatePassword(newPassword: String, onComplete: (Boolean) -> Unit) {
        val user = auth.currentUser ?: return onComplete(false)

        user.updatePassword(newPassword)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Actualiza la URL de la imagen de perfil.
     */
    fun updateProfileImageUrl(uid: String, imageUrl: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("User").document(uid)
            .update("profileImageUrl", imageUrl)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Elimina el avatar (vuelve la URL vacía).
     */
    fun deleteAvatar(uid: String, onComplete: (Boolean) -> Unit) {
        firestore.collection("User").document(uid)
            .update("profileImageUrl", "")
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Cambia el estado de cuenta privada.
     */
    fun updatePrivacy(uid: String, isPrivate: Boolean, onComplete: (Boolean) -> Unit) {
        firestore.collection("User").document(uid)
            .update("privacy", isPrivate)

            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    // Puedes agregar más funciones aquí: actualización de código de amigo, fecha de registro, etc.
}
