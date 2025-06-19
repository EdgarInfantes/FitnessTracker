package dev.einfantesv.fitnesstracker.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseGetDataManager {

    fun getAvatarUrls(onResult: (List<String>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Avatars") // Asegúrate de que esta colección exista
            .get()
            .addOnSuccessListener { result ->
                val urls = result.documents.mapNotNull { it.getString("url") }
                onResult(urls)
            }
            .addOnFailureListener {
                onResult(emptyList()) // En caso de error, devuelve lista vacía
            }
    }
}