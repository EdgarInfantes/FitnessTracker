package dev.einfantesv.fitnesstracker

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel

class StepCounterViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    val stepCount: MutableState<Int> = mutableStateOf(0)
    private var isListening = false

    fun startListening() {
        if (!isListening) {
            stepDetectorSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) //cambiar normal a fastest
                isListening = true
            }
        }
    }

    fun stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this)
            isListening = false
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            // Actualizar contador en el hilo principal para seguridad (aunque mutableState es seguro)
            val steps = event.values[0].toInt()
            if (steps > 0) {
                // Asegurarse que se incremente rápido y sin delay
                stepCount.value += steps
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
