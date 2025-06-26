package dev.einfantesv.fitnesstracker.Screens.home

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import android.view.ViewGroup
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import java.text.NumberFormat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.core.graphics.toColorInt
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.firebase.auth.FirebaseAuth
import dev.einfantesv.fitnesstracker.Screens.util.AnimatedSnackbar
import dev.einfantesv.fitnesstracker.Screens.util.Headers
import dev.einfantesv.fitnesstracker.Screens.util.coloresDegradados
import dev.einfantesv.fitnesstracker.StepCounterViewModel
import dev.einfantesv.fitnesstracker.data.remote.firebase.FirebaseGetDataManager
import java.util.Locale

@Composable
fun DataUserScreen(navController: NavHostController,
                   stepCounterViewModel: StepCounterViewModel) {
    /**
     * Pantalla de visualización de datos del usuario como pasos, calorías, gráfico y resumen.
     *
     * @param navController controlador de navegación
     * @param stepCounterViewModel viewmodel que contiene el contador de pasos y calorías
     */

    var selectedPeriod by remember { mutableStateOf("Semanal") }
    val steps = stepCounterViewModel.weeklySteps.value
    val labels = stepCounterViewModel.weeklyLabels.value
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var meta by remember { mutableFloatStateOf(6000f) }
    var stepsToday by remember { mutableIntStateOf(0) }

    LaunchedEffect(selectedPeriod, uid) {
        FirebaseGetDataManager.getTodaySteps(uid) { steps ->
            stepsToday = steps
        }

        FirebaseGetDataManager.getUserStepGoal(uid) { goal ->
            if (goal != null) {
                val fetchedGoal = goal.toFloat()
                meta = if (selectedPeriod == "Mensual") fetchedGoal * 30 else fetchedGoal
            }
        }

        if (selectedPeriod == "Semanal") {
            stepCounterViewModel.loadWeeklySteps(uid)
        } else {
            stepCounterViewModel.loadMonthlySteps(uid)
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        HeaderDatos(pasos = stepsToday)

        Spacer(modifier = Modifier.height(16.dp))

        PeriodSelector(selected = selectedPeriod) { selectedPeriod = it }

        Spacer(modifier = Modifier.height(24.dp))

        LaunchedEffect(steps, labels, meta) {
            Log.d("DebugData", "Steps: $steps")
            Log.d("DebugData", "Labels: $labels")
            Log.d("DebugData", "Meta: $meta")
        }


        if (steps.isNotEmpty() && labels.isNotEmpty()) {
            CombinedProgressChart(steps = steps, labels = labels, meta = meta)
        }

        Spacer(modifier = Modifier.height(24.dp))

        val (totalPasos, promedioPasos) = promedio(steps, isMonthly = selectedPeriod == "Mensual")

        MuestraResumen(
            total = totalPasos,
            promedio = promedioPasos,
            unidad = "Pasos"
        )
    }
}

@Composable
fun HeaderDatos(pasos: Int) {
    /**
     * Encabezado visual que muestra las calorías quemadas y título principal.
     *
     * @param pasos número de pasos del día (no se usa visualmente aquí)
     * @param calorias calorías quemadas representadas en texto grande
     */

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Headers("Mi Progreso", color = Color(0xFF7948DB))

        Spacer(modifier = Modifier.height(8.dp))

        // Ícono
        Icon(
            imageVector = Icons.Filled.DirectionsWalk,
            contentDescription = "Pasos hoy",
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    scaleX = -1f // reflejo horizontal
                },
            tint = Color.Black
        )

        //Texto Calorias
        Text(
            text = "$pasos",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 100.sp),
            color = Color(0xFF7948DB),
        )

        //Texto Kcal
        Text(
            text = "Pasos de hoy",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
            color = Color(0xFF675B5B)
        )
    }
}

@Composable
fun PeriodSelector(selected: String, onSelect: (String) -> Unit) {
    /**
     * Selector visual entre períodos disponibles ("Semanal", "Mensual").
     *
     * @param selected valor actualmente seleccionado
     * @param onSelect callback que se ejecuta al seleccionar un nuevo valor
     */

    val periods = listOf("Semanal", "Mensual")
    var expanded by remember { mutableStateOf(false) }
    val brush = coloresDegradados(listOf(Color(0xFF7948DB), Color(0xFFFF0000))) //Morado a Rojo

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Texto fijo a la izquierda
        Text(
            text = "Progreso",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black
        )

        // Botón degradado con ícono y texto
        Box {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush = brush)
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selected,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expandir",
                        tint = Color.White
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                periods.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(
                                it,
                                color = Color(0xFF333333) // gris oscuro
                            )
                        },
                        onClick = {
                            onSelect(it)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CombinedProgressChart(steps: List<Float>, labels: List<String>, meta: Float) {
    var selectedIndex by remember { mutableIntStateOf(steps.lastIndex) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarColor by remember { mutableStateOf(Color.Green) }

    val isMonthly = labels.any { it.length >= 3 && it[2] == '.' } || labels.any { it.length == 3 } // Detectar si es mensual por el formato de etiquetas

    AndroidView(
        factory = { context ->
            CombinedChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                isDoubleTapToZoomEnabled = false
                setScaleEnabled(false)
                legend.isEnabled = false
                axisLeft.isEnabled = false

                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.x?.toInt()?.let { index ->
                            selectedIndex = index
                            if (selectedIndex in steps.indices && steps[selectedIndex] >= meta) {
                                snackbarMessage = "Superaste la meta"
                                snackbarColor = Color(0xFF4CAF50)
                            } else {
                                snackbarMessage = "No te rindas, puedes conseguirlo"
                                snackbarColor = Color(0xFFF44336)
                            }
                            showSnackbar = true
                        }
                    }

                    override fun onNothingSelected() {
                        showSnackbar = false
                    }
                })

                animateX(1000)
            }
        },
        update = { chart ->
            val rawMax = maxOf(steps.maxOrNull() ?: 0f, meta)
            val increment = if (isMonthly) 300 else 10
            val maxY = ((rawMax / increment).toInt() + 1) * increment.toFloat()

            chart.axisRight.apply {
                axisMinimum = 0f
                axisMaximum = maxY
                labelCount = 5
                textSize = 12f
                setDrawGridLines(true)
                removeAllLimitLines()

                val limitLine = LimitLine(meta, "Meta")
                limitLine.lineColor = android.graphics.Color.GRAY
                limitLine.lineWidth = 1.5f
                limitLine.labelPosition = LimitLine.LimitLabelPosition.LEFT_TOP
                limitLine.textSize = 12f
                limitLine.enableDashedLine(10f, 10f, 0f)
                addLimitLine(limitLine)
            }

            chart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(true)
                enableGridDashedLine(10f, 10f, 0f)
                setDrawLabels(true)
                textSize = 10f
                labelCount = labels.size
                labelRotationAngle = 0f
                setAvoidFirstLastClipping(true)
                valueFormatter = IndexAxisValueFormatter(labels)
            }

            val lineEntries = steps.mapIndexed { index, value ->
                Entry(index.toFloat(), value)
            }

            val lineDataSet = LineDataSet(lineEntries, "Pasos").apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = "#7948DB".toColorInt()
                setCircleColor("#7948DB".toColorInt())
                setDrawCircles(true)
                circleRadius = 5f
                lineWidth = 2f
                setDrawFilled(false)
                valueTextSize = 10f
                axisDependency = YAxis.AxisDependency.RIGHT
            }

            val highlightEntry = BarEntry(selectedIndex.toFloat(), steps[selectedIndex])
            val barDataSet = BarDataSet(listOf(highlightEntry), "Resaltado").apply {
                setDrawValues(false)
                setGradientColor(0xFF7948DB.toInt(), 0xFFFFFFFF.toInt())
                barShadowColor = android.graphics.Color.TRANSPARENT
                axisDependency = YAxis.AxisDependency.RIGHT
            }

            val combinedData = CombinedData().apply {
                setData(LineData(lineDataSet))
                setData(BarData(barDataSet))
            }

            chart.data = combinedData
            chart.notifyDataSetChanged()
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    )

    AnimatedSnackbar(
        visible = showSnackbar,
        message = snackbarMessage,
        backgroundColor = snackbarColor
    )

    LaunchedEffect(showSnackbar) {
        if (showSnackbar) {
            kotlinx.coroutines.delay(2000)
            showSnackbar = false
        }
    }
}

@Composable
fun MuestraResumen(total: Int, promedio: Int, unidad: String = "Kcal") {
    /**
     * Muestra un recuadro con el total y el promedio de pasos o calorías.
     *
     * @param total valor total acumulado
     * @param promedio valor promedio calculado
     * @param unidad unidad a mostrar ("Pasos", "Kcal", etc.)
     */
    val formattedTotal = NumberFormat.getNumberInstance(Locale.US).format(total)
    val formattedPromedio = NumberFormat.getNumberInstance(Locale.US).format(promedio)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = coloresDegradados(listOf(Color(0xFF7948DB), Color(0xFFFFFFFF), Color(0xFF7948DB))),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(vertical = 12.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$formattedTotal $unidad",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Gray
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$formattedPromedio $unidad",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Black
            )
            Text(
                text = "Promedio",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.Gray
            )
        }
    }
}


fun promedio(valores: List<Float>, isMonthly: Boolean): Pair<Int, Int> {
    /**
     * Calcula el total y el promedio de una lista de valores.
     *
     * @param valores lista de pasos o calorías
     * @param isMonthly true si el período es mensual, false si es semanal
     * @return Pair con total y promedio de valores
     */
    val total = valores.sum().toInt()
    val cantidad = valores.size
    val promedio = if (cantidad > 0) total / cantidad else 0
    return Pair(total, promedio)
}
