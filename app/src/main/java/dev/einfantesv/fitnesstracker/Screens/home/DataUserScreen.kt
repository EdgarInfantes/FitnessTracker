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
import androidx.compose.runtime.mutableIntStateOf
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
    val stepsToday = stepCounterViewModel.stepCount.value
    val calories = stepCounterViewModel.calories.value
    val steps = stepCounterViewModel.weeklySteps.value
    val labels = stepCounterViewModel.weeklyLabels.value
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var meta by remember { mutableStateOf(6000f) }
    
    LaunchedEffect(selectedPeriod, uid) {
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

        Headers("Mi Progreso", color = Color(0xFF7948DB))

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

//@Composable
//fun HeaderDatos(pasos: Int, calorias: Float) {
//    /**
//     * Encabezado visual que muestra las calorías quemadas y título principal.
//     *
//     * @param pasos número de pasos del día (no se usa visualmente aquí)
//     * @param calorias calorías quemadas representadas en texto grande
//     */
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//    ) {
//        Headers("Pasos de Hoy", color = Color(0xFF7948DB))
//
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Ícono
//        Icon(
//            imageVector = Icons.Filled.LocalFireDepartment,
//            contentDescription = "Calorías",
//            modifier = Modifier
//                .size(32.dp)
//                .graphicsLayer {
//                    scaleX = -1f // reflejo horizontal
//                },
//            tint = Color.Black
//        )
//
//        //Texto Calorias
//        Text(
//            text = "${calorias.toInt()}",
//            style = MaterialTheme.typography.titleLarge.copy(fontSize = 100.sp),
//            color = Color(0xFF7948DB),
//        )
//
//        //Texto Kcal
//        Text(
//            text = "Kcal",
//            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
//            color = Color(0xFF675B5B)
//        )
//    }
//}

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
    /**
     * Gráfico combinado que muestra los pasos diarios/mensuales con una línea y una barra
     * para el valor seleccionado. También muestra una línea de meta.
     *
     * @param steps lista de valores de pasos
     * @param labels etiquetas para el eje X (fechas o meses)
     * @param meta valor de meta diaria o mensual
     */

    var selectedIndex by remember { mutableStateOf(steps.lastIndex) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarColor by remember { mutableStateOf(Color.Green) }


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
            // Preparar datos
            val maxY = maxOf(steps.maxOrNull() ?: 0f, meta * 1.1f)
            val minY = minOf(steps.minOrNull() ?: 0f, meta * 0.9f)

            // Configurar eje Y
            chart.axisRight.apply {
                axisMinimum = minY
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

            // Configurar eje X con etiquetas multilínea
            chart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setDrawLabels(true)
                textSize = 10f
                labelCount = labels.size
                labelRotationAngle = -90f
                setAvoidFirstLastClipping(true)
                valueFormatter = IndexAxisValueFormatter(labels)
            }

            // Línea de pasos
            val lineEntries = steps.mapIndexed { index, value ->
                Entry(index.toFloat(), value)
            }

            val lineDataSet = LineDataSet(lineEntries, "Pasos").apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = "#7948DB".toColorInt()
                setCircleColor("#7948DB".toColorInt())

                color = android.graphics.Color.parseColor("#7948DB")
                setCircleColor(android.graphics.Color.parseColor("#7948DB"))

                setDrawCircles(true)
                circleRadius = 5f
                lineWidth = 2f
                setDrawFilled(false)
                valueTextSize = 10f
                axisDependency = YAxis.AxisDependency.RIGHT
            }

            // Barra resaltada
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

            if (selectedIndex in steps.indices && steps[selectedIndex] >= meta) {
                snackbarMessage = "Superaste la meta"
                snackbarColor = Color(0xFF4CAF50)
            }else{
                snackbarMessage = "No te rindas, puedes conseguirlo"
                snackbarColor = Color(0xFFF44336)

            }
            showSnackbar = true

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

/*
@Composable
fun CombinedProgressChart(
    steps: List<Float>,
    labels: List<String>,
    meta: Float
) {
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    var snackbarColor by remember { mutableStateOf(Color.Green) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    AndroidView(
        factory = { context ->
            CombinedChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor("#F9F9F9".toColorInt())
                description.isEnabled = false
                setTouchEnabled(true)
                isDoubleTapToZoomEnabled = false
                setScaleEnabled(false)
                legend.isEnabled = false
                axisLeft.isEnabled = false
                axisRight.textSize = 12f
                axisRight.gridColor = android.graphics.Color.LTGRAY
                xAxis.setDrawAxisLine(true)
                xAxis.setDrawGridLines(true)
                xAxis.gridLineWidth = 1f
                xAxis.gridColor = android.graphics.Color.LTGRAY
                animateX(500)

                setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {
                        e?.let {
                            val index = it.x.toInt()
                            selectedIndex = index
                            if (steps[index] >= meta) {
                                snackbarMessage = "Superaste la meta"
                                snackbarColor = Color(0xFF4CAF50)
                            } else {
                                snackbarMessage = "No superaste la meta"
                                snackbarColor = Color(0xFFF44336)
                            }
                            showSnackbar = true
                        }
                    }

                    override fun onNothingSelected() {}
                })
            }
        },
        update = { chart ->
            val maxStep = steps.maxOrNull() ?: 0f
            val roundedMax = ((maxStep + 500) / 500).toInt() * 500f
            val maxY = maxOf(roundedMax, meta)

            val minStep = steps.minOrNull() ?: 0f
            val roundedMin = ((minStep - 500) / 500).toInt() * 500f
            val minY = minOf(roundedMin, meta)

            Log.e("DEBUG_DATA", "Minimo $minY, Aximo $maxY" )

            chart.axisRight.apply {
                axisMinimum = minY
                axisMaximum = maxY
                labelCount = 5
                granularity = 1f
                removeAllLimitLines()
                addLimitLine(LimitLine(meta, meta.toInt().toString()).apply {
                    lineColor = android.graphics.Color.RED
                    lineWidth = 2f
                    textColor = android.graphics.Color.RED
                    textSize = 12f
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                })
            }

            chart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(true)
                setDrawLabels(true)
                valueFormatter = IndexAxisValueFormatter(labels)
            }

            val lineEntries = steps.mapIndexed { i, v -> Entry(i.toFloat(), v) }
            val lineSet = LineDataSet(lineEntries, "Pasos").apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = "#7948DB".toColorInt()
                setCircleColor("#7948DB".toColorInt())
                circleRadius = 6f
                lineWidth = 2f
                setDrawFilled(true)
                fillColor = "#D1C4E9".toColorInt()
                fillAlpha = 100
                valueTextSize = 10f
                axisDependency = YAxis.AxisDependency.RIGHT
            }

            val barEntry = BarEntry(selectedIndex.toFloat(), steps[selectedIndex])
            val barSet = BarDataSet(listOf(barEntry), "Seleccionado").apply {
                setDrawValues(false)
                setGradientColor(android.graphics.Color.parseColor("#7948DB"), android.graphics.Color.parseColor("#FF4081"))
                axisDependency = YAxis.AxisDependency.RIGHT
            }

            val combined = CombinedData().apply {
                setData(LineData(lineSet))
                setData(BarData(barSet))
            }

            chart.data = combined
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
*/

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
