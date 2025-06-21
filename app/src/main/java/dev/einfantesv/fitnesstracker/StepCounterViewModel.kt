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
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseGetDataManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepCounterViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager: SensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    private val prefs = application.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
    private val todayDateKey = getTodayDateKey()

    var stepCount by mutableIntStateOf(prefs.getInt("${todayDateKey}_stepCount", 0))
        private set
    var stepsToday by mutableIntStateOf(prefs.getInt("${todayDateKey}_stepsToday", 0))
        private set
    var elapsedMinutes by mutableDoubleStateOf(prefs.getFloat("${todayDateKey}_elapsedMinutes", 0f).toDouble())
        private set
    var caloriesToday by mutableDoubleStateOf(prefs.getFloat("${todayDateKey}_caloriesToday", 0f).toDouble())
        private set
    var distanceToday by mutableDoubleStateOf(prefs.getFloat("${todayDateKey}_distanceToday", 0f).toDouble())
        private set

    private var lastStepTime: Long = 0L

    init {
        startListening() // Opcionalmente puede quitarse si deseas controlarlo desde HomeScreen
    }

    fun startListening() {
        stepDetectorSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    private fun getTodayDateKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
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
                    val diffMillis = currentTime - lastStepTime
                    val diffMinutes = diffMillis / 60000.0
                    elapsedMinutes += diffMinutes
                }
                lastStepTime = currentTime

                saveValues()
            }
        }
    }

    private fun saveValues() {
        prefs.edit().apply {
            putInt("${todayDateKey}_stepCount", stepCount)
            putInt("${todayDateKey}_stepsToday", stepsToday)
            putFloat("${todayDateKey}_elapsedMinutes", elapsedMinutes.toFloat())
            putFloat("${todayDateKey}_caloriesToday", caloriesToday.toFloat())
            putFloat("${todayDateKey}_distanceToday", distanceToday.toFloat())
            apply()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }

    private fun calculateCalories(steps: Int): Double {
        return steps * 0.0288
    }

    private fun calculateDistance(steps: Int): Double {
        return steps * 0.0008
    }

    // Data class para los datos de pasos
    data class StepDataResult(
        val steps: List<Float>,
        val labels: List<String>
    )

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
}
