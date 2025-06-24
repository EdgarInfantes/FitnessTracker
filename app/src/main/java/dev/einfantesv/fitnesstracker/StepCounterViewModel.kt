package dev.einfantesv.fitnesstracker

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.FirebaseAuth
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseGetDataManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepCounterViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager: SensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val prefs = application.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
    //private val todayDateKey get() = getTodayDateKey()

    var currentUid: String = ""

    var stepCount by mutableIntStateOf(0)
        private set
    var stepsToday by mutableIntStateOf(0)
        private set
    var elapsedMinutes by mutableDoubleStateOf(0.0)
        private set
    var caloriesToday by mutableDoubleStateOf(0.0)
        private set
    var distanceToday by mutableDoubleStateOf(0.0)
        private set
    // Variables para almacenar pasos y etiquetas de la gráfica semanal o mensual
    private val _weeklySteps = mutableStateOf<List<Float>>(emptyList())
    val weeklySteps: State<List<Float>> = _weeklySteps
    private val _weeklyLabels = mutableStateOf<List<String>>(emptyList())
    val weeklyLabels: State<List<String>> = _weeklyLabels

    // Cargar últimos 7 días
    fun loadWeeklySteps(uid: String) {
        FirebaseGetDataManager.getStepsLast7Days(uid) { result ->
            _weeklySteps.value = result.steps
            _weeklyLabels.value = result.labels
        }
    }

    // Cargar últimos 7 meses
    fun loadMonthlySteps(uid: String) {
        FirebaseGetDataManager.getStepsLast7Months(uid) { steps, labels ->
            _weeklySteps.value = steps
            _weeklyLabels.value = labels
            Log.d("DEBUG_MONTH", "Steps: $steps")
            Log.d("DEBUG_MONTH", "Labels: $labels")
        }
    }

    private var lastStepTime: Long = 0L

    init {
        loadLocalData()
        startListening()
    }

    private fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun loadLocalData() {
        stepCount = prefs.getInt("${getTodayDateKey()}_stepCount", 0)
        stepsToday = prefs.getInt("${getTodayDateKey()}_stepsToday", 0)
        elapsedMinutes = prefs.getFloat("${getTodayDateKey()}_elapsedMinutes", 0f).toDouble()
        caloriesToday = prefs.getFloat("${getTodayDateKey()}_caloriesToday", 0f).toDouble()
        distanceToday = prefs.getFloat("${getTodayDateKey()}_distanceToday", 0f).toDouble()
    }

    fun clearLocalStepData() {
        prefs.edit().clear().apply()
        stepCount = 0
        stepsToday = 0
        elapsedMinutes = 0.0
        caloriesToday = 0.0
        distanceToday = 0.0
    }

    fun loadTodayStepsFromFirestore(uid: String) {
        currentUid = uid
        FirebaseGetDataManager.getTodaySteps(uid) { steps ->
            setStepsTodayFromRemote(steps)
            caloriesToday = calculateCalories(steps)
            distanceToday = calculateDistance(steps)
            elapsedMinutes = 0.0
            saveValues()
        }
    }

    fun setStepsTodayFromRemote(value: Int) {
        stepsToday = value
    }

    private fun saveValues() {
        prefs.edit().apply {
            putInt("${getTodayDateKey()}_stepCount", stepCount)
            putInt("${getTodayDateKey()}_stepsToday", stepsToday)
            putFloat("${getTodayDateKey()}_elapsedMinutes", elapsedMinutes.toFloat())
            putFloat("${getTodayDateKey()}_caloriesToday", caloriesToday.toFloat())
            putFloat("${getTodayDateKey()}_distanceToday", distanceToday.toFloat())
            apply()
        }
    }

    fun startListening() {
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    // Data class para los datos de pasos
    data class StepDataResult(
        val steps: List<Float>,
        val labels: List<String>
    )

    fun onUserLogin(uid: String) {
        currentUid = uid
        stopListening() // Para evitar eventos del usuario anterior
        clearLocalStepData() // Limpiar SharedPreferences local
        loadTodayStepsFromFirestore(uid) // Cargar pasos desde Firestore
        startListening() // Reanudar escucha con el nuevo usuario
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            val steps = event.values[0].toInt()
            if (steps > 0) {
                stepCount += steps
                stepsToday += steps
                caloriesToday = calculateCalories(stepsToday)
                distanceToday = calculateDistance(stepsToday)

                val currentTime = System.currentTimeMillis()
                if (lastStepTime != 0L) {
                    val diffMinutes = (currentTime - lastStepTime) / 60000.0
                    elapsedMinutes += diffMinutes
                }
                lastStepTime = currentTime

                saveValues()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    private fun calculateCalories(steps: Int) = steps * 0.0288
    private fun calculateDistance(steps: Int) = steps * 0.0008
}
