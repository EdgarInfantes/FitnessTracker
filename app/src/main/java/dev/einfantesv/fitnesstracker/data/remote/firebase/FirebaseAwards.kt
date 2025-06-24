package dev.einfantesv.fitnesstracker.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseAwards {

    private val firestore = FirebaseFirestore.getInstance()

    data class Award(
        val description: String = "",
        val url: String = ""
    )

    fun getAwards(onResult: (List<Award>) -> Unit) {
        firestore.collection("Awards")
            .get()
            .addOnSuccessListener { result ->
                val awards = result.documents.mapNotNull { doc ->
                    val description = doc.getString("description")
                    val url = doc.getString("url")
                    if (description != null && url != null) {
                        Award(description, url)
                    } else null
                }
                onResult(awards)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}
