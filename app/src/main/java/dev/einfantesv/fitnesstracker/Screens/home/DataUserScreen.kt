package dev.einfantesv.fitnesstracker.screens.home

import android.R.attr.scaleX
import android.graphics.drawable.GradientDrawable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import java.text.NumberFormat
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.LineChart
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
import androidx.compose.ui.graphics.Brush
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.CombinedChart
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import dev.einfantesv.fitnesstracker.Screens.util.Headers
import dev.einfantesv.fitnesstracker.Screens.util.coloresDegradados
import dev.einfantesv.fitnesstracker.StepCounterViewModel
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
    val stepsToday = stepCounterViewModel.stepCount.value
    val calories = stepCounterViewModel.calories.value

    val stepsLabelsPair by remember(selectedPeriod) {
        mutableStateOf(
            when (selectedPeriod) {
                "Semanal" -> generateWeeklySteps()
                "Mensual" -> generateMonthlySteps()
                else -> generateWeeklySteps()
            }
        )
    }

    val labels = stepsLabelsPair.first
    val steps = stepsLabelsPair.second


    val baseMeta = 6000f
    val meta = when (selectedPeriod) {
        "Mensual" -> baseMeta * 30
        else -> baseMeta
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        //Header de la pantalla Datos
        headerDatos(pasos = stepsToday, calorias = calories.toFloat())


        Spacer(modifier = Modifier.height(16.dp))

        //Texto para mostrar la cantidad de pasos
        Text(
            text = "Pasos: $stepsToday",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF7948DB)
        )


        PeriodSelector(selected = selectedPeriod) { selectedPeriod = it }

        Spacer(modifier = Modifier.height(24.dp))

        CombinedProgressChart(steps = steps, labels = labels, meta = meta)

        Spacer(modifier = Modifier.height(24.dp))

        val (totalPasos, promedioPasos) = Promedio(steps, isMonthly = selectedPeriod == "Mensual")

        muestraResumen(
            total = totalPasos,
            promedio = promedioPasos,
            unidad = "Pasos"
        )


    }
}

@Composable
fun headerDatos(pasos: Int, calorias: Float) {
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
        Headers("Pasos de Hoy", color = Color(0xFF7948DB))

        Spacer(modifier = Modifier.height(8.dp))

        // Ícono
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = "Calorías",
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer {
                    scaleX = -1f // reflejo horizontal
                },
            tint = Color.Black
        )

        //Texto Calorias
        Text(
            text = "${calorias.toInt()}",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 100.sp),
            color = Color(0xFF7948DB),
        )

        //Texto Kcal
        Text(
            text = "Kcal",
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
    /**
     * Gráfico combinado que muestra los pasos diarios/mensuales con una línea y una barra
     * para el valor seleccionado. También muestra una línea de meta.
     *
     * @param steps lista de valores de pasos
     * @param labels etiquetas para el eje X (fechas o meses)
     * @param meta valor de meta diaria o mensual
     */

    var selectedIndex by remember { mutableStateOf(steps.lastIndex) }
    var showPopup by remember { mutableStateOf(false) }

    if (showPopup) {
        AlertDialog(
            onDismissRequest = { showPopup = false },
            confirmButton = {
                TextButton(onClick = { showPopup = false }) {
                    Text("OK")
                }
            },
            title = { Text("¡Bien Hecho!") },
            text = { Text("Has superado la meta.") }
        )
    }

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
                        showPopup = false
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

            if (selectedIndex in steps.indices && steps[selectedIndex] > meta) {
                showPopup = true
            }

            chart.data = combinedData
            chart.notifyDataSetChanged()
            chart.invalidate()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    )
}

@Composable
fun muestraResumen(total: Int, promedio: Int, unidad: String = "Kcal") {
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
                brush = coloresDegradados(listOf(Color(0xFFFDEEEE), Color(0xFFFFFFFF), Color(0xFFFDEEEE))),
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


fun Promedio(valores: List<Float>, isMonthly: Boolean): Pair<Int, Int> {
    /**
     * Calcula el total y el promedio de una lista de valores.
     *
     * @param valores lista de pasos o calorías
     * @param isMonthly true si el período es mensual, false si es semanal
     * @return Pair con total y promedio de valores
     */
    val total = valores.sum().toInt()
    val dias = if (isMonthly) LocalDate.now().lengthOfMonth() else 7
    val promedio = if (dias > 0) total / dias else 0
    return Pair(total, promedio)
}


fun generateWeeklySteps(): Pair<List<String>, List<Float>> {
    /**
     * Genera etiquetas y valores de pasos simulados para los últimos 7 días.
     *
     * @return Pair de lista de etiquetas con fecha y lista de valores de pasos
     */
    val today = LocalDate.now()
    val dayFormatter = DateTimeFormatter.ofPattern("EEE", Locale("es"))
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("es"))
    val days = (0..6).map { today.minusDays((6 - it).toLong()) }
    val labels = days.map { "${it.format(dayFormatter)} \n ${it.format(dateFormatter)}" }
    val steps = listOf(4500f, 6200f, 5800f, 7200f, 5000f, 6900f, 6100f)
    return Pair(labels, steps)
}

fun generateMonthlySteps(): Pair<List<String>, List<Float>> {
    /**
     * Genera etiquetas y pasos simulados para los últimos 7 meses.
     *
     * @return Pair con etiquetas de meses y valores de pasos por mes
     */

    // Formato con salto de línea: "Ene\n2025" y en español
    val formatter = DateTimeFormatter.ofPattern("MMM \n yyyy", Locale("es"))

    // Mes actual
    val thisMonth = YearMonth.now()

    // Últimos 6 meses (desde hace 5 hasta este mes)
    val months = (0..6).map { thisMonth.minusMonths((6 - it).toLong()) }

    // Convertimos YearMonth a LocalDate (día 1) para poder formatearlo
    val labels = months.map { it.atDay(1).format(formatter) }

    // Valores de pasos, debe tener exactamente la misma longitud que labels (6)
    val steps = listOf(110000f, 120000f, 135000f, 118000f, 142000f, 130000f, 190000f)

    return Pair(labels, steps)
}
