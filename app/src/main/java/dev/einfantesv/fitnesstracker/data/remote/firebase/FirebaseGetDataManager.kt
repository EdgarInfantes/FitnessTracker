package dev.einfantesv.fitnesstracker.data.remote.firebase

import android.annotation.SuppressLint
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.google.firebase.Timestamp
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import dev.einfantesv.fitnesstracker.UserModel
import java.time.Month
import java.time.ZoneId
import java.util.Date
import java.time.format.TextStyle
import java.util.Calendar

object FirebaseGetDataManager {

    @SuppressLint("StaticFieldLeak")

    private val firestore = FirebaseFirestore.getInstance()

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

    fun getStepsLast7Days(uid: String, callback: (StepCounterViewModel.StepDataResult) -> Unit) {
        val sevenDaysAgo = LocalDate.now().minusDays(7)
        val startOfDay = sevenDaysAgo.atStartOfDay(ZoneId.systemDefault())
        val date = Date.from(startOfDay.toInstant())
        val startTimestamp = Timestamp(date)
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("Steps")
            .whereEqualTo("uid", uid)
            .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
            .get()
            .addOnSuccessListener { result ->
                val grouped = mutableMapOf<String, Int>()

                for (doc in result.documents) {
                    val timestamp = doc.getTimestamp("timestamp")?.toDate() ?: continue
                    val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(timestamp)
                    val steps = doc.getLong("steps")?.toInt() ?: 0
                    grouped[dateKey] = grouped.getOrDefault(dateKey, 0) + steps
                }

                // Generar lista ordenada de fechas
                val stepsList = mutableListOf<Float>()
                val labelsList = mutableListOf<String>()

                for (i in 1..7) {
                    val date = sevenDaysAgo.plusDays(i.toLong())
                    val key = date.toString()
                    val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("es"))
                    val formattedDate = "${dayName.lowercase()}, ${date.format(DateTimeFormatter.ofPattern("dd/MM"))}"
                    labelsList.add(formattedDate)

                    stepsList.add(grouped[key]?.toFloat() ?: 0f)
                }

                callback(StepCounterViewModel.StepDataResult(stepsList, labelsList))
            }
            .addOnFailureListener {
                Log.e("FIREBASE_ERROR", "Error al recuperar pasos: ${it.message}")
                callback(StepCounterViewModel.StepDataResult(emptyList(), emptyList()))
            }
    }



    fun getStepsLast7Months(uid: String, callback: (List<Float>, List<String>) -> Unit) {
        val now = LocalDate.now()
        val firestore = FirebaseFirestore.getInstance()
        val stepsByMonth = mutableMapOf<Int, Int>() // monthNumber -> totalSteps

        // Obtener desde hace 6 meses + mes actual
        val startDate = now.minusMonths(6).withDayOfMonth(1)
        val startOfDay = startDate.atStartOfDay(ZoneId.systemDefault())
        val startTimestamp = Timestamp(Date.from(startOfDay.toInstant()))


        firestore.collection("Steps")
            .whereEqualTo("uid", uid)
            .whereGreaterThanOrEqualTo("timestamp", startTimestamp)
            .get()
            .addOnSuccessListener { result ->
                for (doc in result.documents) {
                    val timestamp = doc.getTimestamp("timestamp")?.toDate() ?: continue
                    val steps = doc.getLong("steps")?.toInt() ?: 0
                    val localDate = timestamp.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    val month = localDate.monthValue
                    stepsByMonth[month] = stepsByMonth.getOrDefault(month, 0) + steps
                }

                // Generar listas ordenadas
                val months = (0..6).map { now.minusMonths(it.toLong()).monthValue }.reversed()
                val monthLabels = months.map {
                    Month.of(it).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                }
                val stepValues = months.map { stepsByMonth[it]?.toFloat() ?: 0f }

                callback(stepValues, monthLabels)
            }
            .addOnFailureListener {
                Log.e("DEBUG_FIREBASE", "Error al obtener pasos mensuales", it)
                callback(emptyList(), emptyList())
            }
    }


    fun getUserStepGoal(uid: String, onResult: (Int?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("User")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val goal = document.getLong("dailyGoal")?.toInt()

                onResult(goal)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

    fun saveStepsIfFirstWalkToday(
        uid: String,
        steps: Int,
        acTime: Double,
        acCalories: Double,
        acDistance: Double
    ) {
        val db = FirebaseFirestore.getInstance()

        // Crear Timestamp del día actual con hora 00:00:00
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayTimestamp = Timestamp(calendar.time)

        val stepData = hashMapOf(
            "uid" to uid,
            "steps" to steps,
            "acTime" to acTime,
            "acCalories" to acCalories,
            "acDistance" to acDistance,
            "timestamp" to todayTimestamp
        )

        // Buscar si ya existe un documento para este uid y fecha
        db.collection("Steps")
            .whereEqualTo("uid", uid)
            .whereEqualTo("timestamp", todayTimestamp)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No existe, se crea nuevo
                    db.collection("Steps")
                        .add(stepData)
                        .addOnSuccessListener {
                            Log.d("Firestore Steps Update", "Uid: $uid, Pasos: $steps")
                            Log.d("Firestore Steps Update", "Documento creado correctamente para hoy.")
                        }
                        .addOnFailureListener { e ->
                            Log.d("Firestore Steps Update", "Uid: $uid, Pasos: $steps")
                            Log.e("Firestore Steps Update", "Error al guardar documento: ${e.message}")
                        }
                } else {
                    // Ya existe, se actualiza
                    val docRef = documents.first().reference
                    docRef.update(stepData as Map<String, Any>)
                        .addOnSuccessListener {
                            Log.d("Firestore Steps Update", "Uid: $uid, Pasos: $steps")
                            Log.d("Firestore Steps Update", "Documento actualizado correctamente para hoy.")
                        }
                        .addOnFailureListener { e ->
                            Log.d("Firestore Steps Update", "Uid: $uid, Pasos: $steps")
                            Log.e("Firestore Steps Update", "Error al actualizar documento: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error al verificar documento existente: ${e.message}")
            }
    }

    fun getTodaySteps(uid: String, onResult: (Int) -> Unit) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayTimestamp = Timestamp(calendar.time)

        FirebaseFirestore.getInstance()
            .collection("Steps")
            .whereEqualTo("uid", uid)
            .whereEqualTo("timestamp", todayTimestamp)
            .get()
            .addOnSuccessListener { documents ->
                val steps = documents.firstOrNull()?.getLong("steps")?.toInt() ?: 0
                onResult(steps)
            }
            .addOnFailureListener {
                Log.e("Firestore", "Error al obtener pasos de hoy: ${it.message}")
                onResult(0)
            }
    }


    fun getTopStepUsers(limit: Int = 3, onResult: (List<String>) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("Steps")
            .orderBy("steps", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .addOnSuccessListener { result ->
                val topUids = result.documents.mapNotNull { it.getString("uid") }
                onResult(topUids)
            }
            .addOnFailureListener {
                Log.e("FirebaseGetDataManager", "Error al obtener ranking: ${it.message}")
                onResult(emptyList())
            }
    }

    fun getUserByUid(uid: String, onResult: (UserModel?) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("User")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onResult(document.toObject(UserModel::class.java))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                Log.e("FirebaseGetDataManager", "Error al obtener usuario: ${it.message}")
                onResult(null)
            }
    }
}