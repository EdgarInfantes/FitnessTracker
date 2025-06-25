package dev.einfantesv.fitnesstracker.data.remote.firebase

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath

object FirebaseAwards {

    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    data class Award(
        val uid: String = "",
        val description: String = "",
        val url: String = "",
        val unlocked: Boolean = false
    )

    fun getAwardsWithUnlockStatus(onResult: (List<Award>) -> Unit) {
        val currentUser = auth.currentUser ?: return onResult(emptyList())

        firestore.collection("Awards")
            .get()
            .addOnSuccessListener { result ->
                val allAwards = result.documents.mapNotNull { doc ->
                    val uid = doc.id
                    val description = doc.getString("description")
                    val url = doc.getString("url")
                    if (description != null && url != null) {
                        Award(uid = uid, description = description, url = url)
                    } else null
                }

                // Verificar qué logros ha desbloqueado el usuario
                firestore.collection("Detail_user_award")
                    .whereEqualTo("user_uid", currentUser.uid)
                    .get()
                    .addOnSuccessListener { userAwardDocs ->
                        val unlockedAwardIds = userAwardDocs.mapNotNull { it.getString("award_uid") }.toSet()

                        val finalList = allAwards.map { award ->
                            award.copy(unlocked = unlockedAwardIds.contains(award.uid))
                        }

                        onResult(finalList)
                    }
                    .addOnFailureListener {
                        onResult(allAwards.map { it.copy(unlocked = false) }) // Si falla, asumir ninguno desbloqueado
                    }
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getUnlockedAwardsForUser(userUid: String, onResult: (List<Award>) -> Unit) {
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .get()
            .addOnSuccessListener { detailDocs ->
                val awardUids = detailDocs.mapNotNull { it.getString("award_uid") }

                if (awardUids.isEmpty()) {
                    onResult(emptyList())
                    return@addOnSuccessListener
                }

                firestore.collection("Awards")
                    .whereIn(FieldPath.documentId(), awardUids.take(10)) // Firebase limita a 10 por consulta
                    .get()
                    .addOnSuccessListener { awardDocs ->
                        val unlockedAwards = awardDocs.mapNotNull { doc ->
                            val description = doc.getString("description")
                            val url = doc.getString("url")
                            val uid = doc.id
                            if (description != null && url != null) {
                                Award(uid = uid, description = description, url = url, unlocked = true)
                            } else null
                        }
                        onResult(unlockedAwards)
                    }
                    .addOnFailureListener { onResult(emptyList()) }
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}
