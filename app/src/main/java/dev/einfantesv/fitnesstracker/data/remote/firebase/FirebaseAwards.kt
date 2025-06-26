package dev.einfantesv.fitnesstracker.data.remote.firebase

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldPath
import java.util.Calendar

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

    fun oneThousandStepsOneDay(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onComplete(false)
        val userUid = currentUser.uid

        // ID del logro de "1000 pasos en un día"
        val awardId = "2sJAcnf9bVGFX4rQi445"

        // Paso 1: Verificar si el usuario ya desbloqueó este logro
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .whereEqualTo("award_uid", awardId)
            .get()
            .addOnSuccessListener { existingDocs ->
                if (!existingDocs.isEmpty) {
                    // Ya está desbloqueado
                    return@addOnSuccessListener onComplete(true)
                }

                // Paso 2: Verificar si tiene algún documento con steps > 1000
                firestore.collection("Steps")
                    .whereEqualTo("uid", userUid)
                    .get()
                    .addOnSuccessListener { stepsDocs ->
                        val hasQualified = stepsDocs.any { doc ->
                            val steps = doc.getLong("steps") ?: 0
                            steps > 1000
                        }

                        if (hasQualified) {
                            // Paso 3: Desbloquear el logro creando un documento en Detail_user_award
                            val data = mapOf(
                                "user_uid" to userUid,
                                "award_uid" to awardId
                            )

                            firestore.collection("Detail_user_award")
                                .add(data)
                                .addOnSuccessListener {
                                    onComplete(true) // Éxito
                                }
                                .addOnFailureListener {
                                    onComplete(false) // Fallo al guardar
                                }
                        } else {
                            onComplete(false) // No califica aún
                        }
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun surpassMensualGoal(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onComplete(false)
        val userUid = currentUser.uid
        val awardId = "9NS35rn2GHnPmhWihlNP"

        // Paso 1: Verificar si ya tiene el logro
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .whereEqualTo("award_uid", awardId)
            .get()
            .addOnSuccessListener { existingDocs ->
                if (!existingDocs.isEmpty) {
                    return@addOnSuccessListener onComplete(true)
                }

                // Paso 2: Obtener la meta diaria
                firestore.collection("User")
                    .document(userUid)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val dailyGoal = userDoc.getLong("dailyGoal")?.toInt() ?: return@addOnSuccessListener onComplete(false)
                        val monthlyGoal = dailyGoal * 30

                        // Paso 3: Obtener documentos de pasos
                        firestore.collection("Steps")
                            .whereEqualTo("uid", userUid)
                            .get()
                            .addOnSuccessListener { stepsDocs ->
                                // Agrupar pasos por mes (Map<"YYYY-MM", totalSteps>)
                                val monthlySteps = mutableMapOf<String, Int>()

                                for (doc in stepsDocs) {
                                    val timestamp = doc.getTimestamp("timestamp")?.toDate() ?: continue
                                    val steps = doc.getLong("steps")?.toInt() ?: 0

                                    @SuppressLint("SimpleDateFormat")
                                    val key = java.text.SimpleDateFormat("yyyy-MM").format(timestamp)

                                    monthlySteps[key] = (monthlySteps[key] ?: 0) + steps
                                }

                                // Verificar si supera meta mensual en algún mes
                                val qualifies = monthlySteps.values.any { total -> total > monthlyGoal }

                                if (qualifies) {
                                    // Paso 4: Desbloquear logro
                                    val data = mapOf(
                                        "user_uid" to userUid,
                                        "award_uid" to awardId
                                    )

                                    firestore.collection("Detail_user_award")
                                        .add(data)
                                        .addOnSuccessListener {
                                            onComplete(true)
                                        }
                                        .addOnFailureListener {
                                            onComplete(false)
                                        }
                                } else {
                                    onComplete(false)
                                }
                            }
                            .addOnFailureListener { onComplete(false) }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun surpassDailyGoal(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onComplete(false)
        val userUid = currentUser.uid
        val awardId = "GcMxxD2cfYFwdJTE6LKL"

        // Paso 1: Verificar si ya tiene el logro
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .whereEqualTo("award_uid", awardId)
            .get()
            .addOnSuccessListener { existingDocs ->
                if (!existingDocs.isEmpty) {
                    return@addOnSuccessListener onComplete(true)
                }

                // Paso 2: Obtener la meta diaria del usuario
                firestore.collection("User")
                    .document(userUid)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val dailyGoal = userDoc.getLong("dailyGoal")?.toInt() ?: return@addOnSuccessListener onComplete(false)

                        // Paso 3: Revisar documentos de Steps
                        firestore.collection("Steps")
                            .whereEqualTo("uid", userUid)
                            .get()
                            .addOnSuccessListener { stepsDocs ->
                                val qualifies = stepsDocs.any { doc ->
                                    val steps = doc.getLong("steps")?.toInt() ?: 0
                                    steps >= dailyGoal
                                }

                                if (qualifies) {
                                    // Paso 4: Desbloquear logro
                                    val data = mapOf(
                                        "user_uid" to userUid,
                                        "award_uid" to awardId
                                    )

                                    firestore.collection("Detail_user_award")
                                        .add(data)
                                        .addOnSuccessListener {
                                            onComplete(true)
                                        }
                                        .addOnFailureListener {
                                            onComplete(false)
                                        }
                                } else {
                                    onComplete(false)
                                }
                            }
                            .addOnFailureListener { onComplete(false) }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun oneHundredMillionSteps(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onComplete(false)
        val userUid = currentUser.uid
        val awardId = "J0QH1YMsW514ssMQRmRc"

        // Paso 1: Verificar si ya tiene el logro
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .whereEqualTo("award_uid", awardId)
            .get()
            .addOnSuccessListener { existingDocs ->
                if (!existingDocs.isEmpty) {
                    return@addOnSuccessListener onComplete(true)
                }

                // Paso 2: Obtener todos los pasos del usuario
                firestore.collection("Steps")
                    .whereEqualTo("uid", userUid)
                    .get()
                    .addOnSuccessListener { stepsDocs ->
                        val totalSteps = stepsDocs.sumOf { doc ->
                            doc.getLong("steps")?.toLong() ?: 0L
                        }

                        if (totalSteps > 100_000_000L) {
                            // Paso 3: Desbloquear el logro
                            val data = mapOf(
                                "user_uid" to userUid,
                                "award_uid" to awardId
                            )

                            firestore.collection("Detail_user_award")
                                .add(data)
                                .addOnSuccessListener {
                                    onComplete(true)
                                }
                                .addOnFailureListener {
                                    onComplete(false)
                                }
                        } else {
                            onComplete(false)
                        }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun fiveHundredStepsOneDay(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onComplete(false)
        val userUid = currentUser.uid

        // ID del logro de "500 pasos en un día"
        val awardId = "SXjZeeFT6vqVjvGlBBij"

        // Paso 1: Verificar si el usuario ya desbloqueó este logro
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .whereEqualTo("award_uid", awardId)
            .get()
            .addOnSuccessListener { existingDocs ->
                if (!existingDocs.isEmpty) {
                    // Ya está desbloqueado
                    return@addOnSuccessListener onComplete(true)
                }

                // Paso 2: Verificar si tiene algún documento con steps > 500
                firestore.collection("Steps")
                    .whereEqualTo("uid", userUid)
                    .get()
                    .addOnSuccessListener { stepsDocs ->
                        val hasQualified = stepsDocs.any { doc ->
                            val steps = doc.getLong("steps") ?: 0
                            steps > 500
                        }

                        if (hasQualified) {
                            // Paso 3: Desbloquear el logro creando un documento en Detail_user_award
                            val data = mapOf(
                                "user_uid" to userUid,
                                "award_uid" to awardId
                            )

                            firestore.collection("Detail_user_award")
                                .add(data)
                                .addOnSuccessListener {
                                    onComplete(true) // Éxito
                                }
                                .addOnFailureListener {
                                    onComplete(false) // Fallo al guardar
                                }
                        } else {
                            onComplete(false) // No califica aún
                        }
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun tenThousandStepsOneWeek(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onComplete(false)
        val userUid = currentUser.uid
        val awardId = "VUemz67iT7QNGx1QtAXG"

        // Verifica si ya tiene el logro
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .whereEqualTo("award_uid", awardId)
            .get()
            .addOnSuccessListener { existingDocs ->
                if (!existingDocs.isEmpty) {
                    return@addOnSuccessListener onComplete(true)
                }

                // Obtener los documentos de pasos
                firestore.collection("Steps")
                    .whereEqualTo("uid", userUid)
                    .get()
                    .addOnSuccessListener { stepsDocs ->
                        val weeklySteps = mutableMapOf<String, Long>()

                        for (doc in stepsDocs) {
                            val timestamp = doc.getTimestamp("timestamp") ?: continue
                            val calendar = Calendar.getInstance().apply {
                                time = timestamp.toDate()
                            }

                            // Año y número de semana como clave (por ejemplo: "2025-24")
                            val week = calendar.get(Calendar.WEEK_OF_YEAR)
                            val year = calendar.get(Calendar.YEAR)
                            val weekKey = "$year-$week"

                            val steps = doc.getLong("steps") ?: 0L
                            weeklySteps[weekKey] = weeklySteps.getOrDefault(weekKey, 0L) + steps
                        }

                        val unlocked = weeklySteps.values.any { it >= 10_000L }

                        if (unlocked) {
                            val data = mapOf(
                                "user_uid" to userUid,
                                "award_uid" to awardId
                            )

                            firestore.collection("Detail_user_award")
                                .add(data)
                                .addOnSuccessListener {
                                    onComplete(true)
                                }
                                .addOnFailureListener {
                                    onComplete(false)
                                }
                        } else {
                            onComplete(false)
                        }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun oneHundredStepsOneDay(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onComplete(false)
        val userUid = currentUser.uid

        // ID del logro de "100 pasos en un día"
        val awardId = "tCovRTyD6f9wPCRdAgzA"

        // Paso 1: Verificar si el usuario ya desbloqueó este logro
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .whereEqualTo("award_uid", awardId)
            .get()
            .addOnSuccessListener { existingDocs ->
                if (!existingDocs.isEmpty) {
                    // Ya está desbloqueado
                    return@addOnSuccessListener onComplete(true)
                }

                // Paso 2: Verificar si tiene algún documento con steps > 100
                firestore.collection("Steps")
                    .whereEqualTo("uid", userUid)
                    .get()
                    .addOnSuccessListener { stepsDocs ->
                        val hasQualified = stepsDocs.any { doc ->
                            val steps = doc.getLong("steps") ?: 0
                            steps > 100
                        }

                        if (hasQualified) {
                            // Paso 3: Desbloquear el logro creando un documento en Detail_user_award
                            val data = mapOf(
                                "user_uid" to userUid,
                                "award_uid" to awardId
                            )

                            firestore.collection("Detail_user_award")
                                .add(data)
                                .addOnSuccessListener {
                                    onComplete(true) // Éxito
                                }
                                .addOnFailureListener {
                                    onComplete(false) // Fallo al guardar
                                }
                        } else {
                            onComplete(false) // No califica aún
                        }
                    }
                    .addOnFailureListener {
                        onComplete(false)
                    }
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun oneMillionStepsInAMonth(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onComplete(false)
        val userUid = currentUser.uid
        val awardId = "uxItpKHs9vINBZ6Y2lrq"

        // Verifica si ya tiene el logro
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .whereEqualTo("award_uid", awardId)
            .get()
            .addOnSuccessListener { existingDocs ->
                if (!existingDocs.isEmpty) {
                    return@addOnSuccessListener onComplete(true)
                }

                // Obtener documentos de Steps
                firestore.collection("Steps")
                    .whereEqualTo("uid", userUid)
                    .get()
                    .addOnSuccessListener { stepsDocs ->
                        val monthlySteps = mutableMapOf<String, Long>()

                        for (doc in stepsDocs) {
                            val timestamp = doc.getTimestamp("timestamp") ?: continue
                            val calendar = Calendar.getInstance().apply {
                                time = timestamp.toDate()
                            }

                            // Clave: año-mes (por ejemplo: "2025-06")
                            val year = calendar.get(Calendar.YEAR)
                            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH es base 0
                            val monthKey = String.format("%04d-%02d", year, month)

                            val steps = doc.getLong("steps") ?: 0L
                            monthlySteps[monthKey] = monthlySteps.getOrDefault(monthKey, 0L) + steps
                        }

                        val unlocked = monthlySteps.values.any { it >= 1_000_000L }

                        if (unlocked) {
                            val data = mapOf(
                                "user_uid" to userUid,
                                "award_uid" to awardId
                            )

                            firestore.collection("Detail_user_award")
                                .add(data)
                                .addOnSuccessListener {
                                    onComplete(true)
                                }
                                .addOnFailureListener {
                                    onComplete(false)
                                }
                        } else {
                            onComplete(false)
                        }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun dailyGoalFiveDaysInARow(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onComplete(false)
        val userUid = currentUser.uid
        val awardId = "xNdeYeoPh6HyaKn1KXeE"

        // Verifica si ya tiene el logro
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .whereEqualTo("award_uid", awardId)
            .get()
            .addOnSuccessListener { existingDocs ->
                if (!existingDocs.isEmpty) {
                    return@addOnSuccessListener onComplete(true)
                }

                // Obtener dailyGoal del usuario
                firestore.collection("User")
                    .document(userUid)
                    .get()
                    .addOnSuccessListener { userDoc ->
                        val dailyGoal = userDoc.getLong("dailyGoal") ?: return@addOnSuccessListener onComplete(false)

                        // Obtener los pasos del usuario
                        firestore.collection("Steps")
                            .whereEqualTo("uid", userUid)
                            .get()
                            .addOnSuccessListener { stepsDocs ->
                                val validDates = stepsDocs.documents
                                    .mapNotNull { doc ->
                                        val timestamp = doc.getTimestamp("timestamp") ?: return@mapNotNull null
                                        val steps = doc.getLong("steps") ?: return@mapNotNull null
                                        if (steps >= dailyGoal) {
                                            val calendar = Calendar.getInstance().apply {
                                                time = timestamp.toDate()
                                                set(Calendar.HOUR_OF_DAY, 0)
                                                set(Calendar.MINUTE, 0)
                                                set(Calendar.SECOND, 0)
                                                set(Calendar.MILLISECOND, 0)
                                            }
                                            calendar.timeInMillis
                                        } else null
                                    }
                                    .distinct()
                                    .sorted()

                                // Buscar al menos 5 días consecutivos
                                var streak = 1
                                for (i in 1 until validDates.size) {
                                    val diff = validDates[i] - validDates[i - 1]
                                    if (diff == 86_400_000L) { // 1 día en milisegundos
                                        streak++
                                        if (streak >= 5) break
                                    } else {
                                        streak = 1
                                    }
                                }

                                if (streak >= 5) {
                                    val data = mapOf(
                                        "user_uid" to userUid,
                                        "award_uid" to awardId
                                    )
                                    firestore.collection("Detail_user_award")
                                        .add(data)
                                        .addOnSuccessListener { onComplete(true) }
                                        .addOnFailureListener { onComplete(false) }
                                } else {
                                    onComplete(false)
                                }
                            }
                            .addOnFailureListener { onComplete(false) }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun makeAFriend(onComplete: (Boolean) -> Unit) {
        val currentUser = auth.currentUser ?: return onComplete(false)
        val userUid = currentUser.uid

        // Paso 1: Verificar si ya tiene el logro
        firestore.collection("Detail_user_award")
            .whereEqualTo("user_uid", userUid)
            .whereEqualTo("award_uid", "a8VeQ3i2UvIqNR87h7KH")
            .get()
            .addOnSuccessListener { existingDocs ->
                if (!existingDocs.isEmpty) {
                    return@addOnSuccessListener onComplete(true)
                }

                // Paso 2: Verificar si está en alguna relación de amistad (como uid_one o uid_second)
                firestore.collection("Friend_relation")
                    .whereEqualTo("uid_one", userUid)
                    .get()
                    .addOnSuccessListener { oneDocs ->
                        if (!oneDocs.isEmpty) {
                            // Desbloquear logro
                            addFriendAward(userUid, onComplete)
                        } else {
                            // Buscar como uid_second
                            firestore.collection("Friend_relation")
                                .whereEqualTo("uid_second", userUid)
                                .get()
                                .addOnSuccessListener { secondDocs ->
                                    if (!secondDocs.isEmpty) {
                                        addFriendAward(userUid, onComplete)
                                    } else {
                                        onComplete(false)
                                    }
                                }
                                .addOnFailureListener { onComplete(false) }
                        }
                    }
                    .addOnFailureListener { onComplete(false) }
            }
            .addOnFailureListener { onComplete(false) }
    }

    private fun addFriendAward(userUid: String, onComplete: (Boolean) -> Unit) {
        val data = mapOf(
            "user_uid" to userUid,
            "award_uid" to "a8VeQ3i2UvIqNR87h7KH"
        )

        firestore.collection("Detail_user_award")
            .add(data)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}
