package com.pruden.habits.modules.estadisticasHabito.metodos

import android.content.Context
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.pruden.habits.R
import com.pruden.habits.common.clases.data.Habito
import com.pruden.habits.common.metodos.General.formatearNumero
import com.pruden.habits.databinding.FragmentEstadisticasBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

fun cargarSpinnerGraficoDeLineas(
    context: Context,
    binding: FragmentEstadisticasBinding,
    habito: Habito,
    formatoFechaOriginal: SimpleDateFormat,
    formatoFecha_dd: SimpleDateFormat
) {
    actualizarGraficoDeLineas(
        context,
        context.getString(R.string.periodo_mes),
        "Mes",
        binding,
        habito,
        formatoFechaOriginal,
        formatoFecha_dd
    )

    val opciones = arrayOf(
        context.getString(R.string.periodo_dia),
        context.getString(R.string.periodo_semana),
        context.getString(R.string.periodo_mes),
        context.getString(R.string.periodo_ano)
    )
    val adapter = ArrayAdapter(context, R.layout.spinner_item, opciones)
    adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
    binding.spinnerEstaLineChart.adapter = adapter
    binding.spinnerEstaLineChart.setSelection(2)

    // Mapa para asociar valores traducidos con valores internos
    val periodoValueMap = mapOf(
        context.getString(R.string.periodo_dia) to "Día",
        context.getString(R.string.periodo_semana) to "Semana",
        context.getString(R.string.periodo_mes) to "Mes",
        context.getString(R.string.periodo_ano) to "Año"
    )

    binding.spinnerEstaLineChart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            val opcionSeleccionadaTraducida = parent?.getItemAtPosition(position).toString()
            val opcionSeleccionadaInterna = periodoValueMap[opcionSeleccionadaTraducida] ?: opcionSeleccionadaTraducida

            binding.lineChart.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, 0f, 0f, 0))
            binding.lineChart.onTouchEvent(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0f, 0f, 0))
            binding.lineChart.highlightValues(null)

            actualizarGraficoDeLineas(
                context,
                opcionSeleccionadaTraducida,
                opcionSeleccionadaInterna,
                binding,
                habito,
                formatoFechaOriginal,
                formatoFecha_dd
            )
        }
        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }
}

private fun actualizarGraficoDeLineas(
    context: Context,
    tiempoTraducido: String,
    tiempoInterno: String,
    binding: FragmentEstadisticasBinding,
    habito: Habito,
    formatoFechaOriginal: SimpleDateFormat,
    formatoFecha_dd: SimpleDateFormat
) {
    CoroutineScope(Dispatchers.IO).launch {
        val xValues: MutableList<String>
        val yValues: MutableList<String>
        var formatoFechaArriba = ""
        var barras = 7f
        var espacio = 0.3f
        var fechaTexto = ""

        when (tiempoInterno) {
            "Día" -> {
                xValues = habito.listaFechas.mapNotNull { fecha ->
                    try {
                        val date = formatoFechaOriginal.parse(fecha)
                        formatoFecha_dd.format(date!!)
                    } catch (e: Exception) {
                        null
                    }
                }.toMutableList()
                yValues = habito.listaValores.toMutableList()

                formatoFechaArriba = "MMM yyyy"

                fechaTexto = if (habito.listaFechas.isNotEmpty()) {
                    val ultimaFecha = formatoFechaOriginal.parse(habito.listaFechas.last())
                    SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(ultimaFecha!!).uppercase()
                } else {
                    context.getString(R.string.sin_datos)
                }
            }
            "Semana" -> {
                val datosSemanales = agruparPorSemanaConRegistros(habito.listaFechas, habito.listaValores)
                xValues = datosSemanales.keys.toMutableList()
                yValues = datosSemanales.values.map { it.toString() }.toMutableList()
                barras = 4f
                espacio = 0.2f

                formatoFechaArriba = "MMM yyyy"

                fechaTexto = if (xValues.isNotEmpty()) {
                    xValues.last().split(" ").last().uppercase().replace("@", " ")
                } else {
                    context.getString(R.string.sin_datos)
                }
            }
            "Mes" -> {
                val datosMensuales = agruparPorMesConRegistros(habito.listaFechas, habito.listaValores)
                xValues = datosMensuales.keys.toMutableList()
                yValues = datosMensuales.values.map { it.toString() }.toMutableList()
                barras = 6f
                espacio = 0.1f

                fechaTexto = if (xValues.isNotEmpty()) {
                    xValues.last().split("@")[1]
                } else {
                    context.getString(R.string.sin_datos)
                }
            }
            "Año" -> {
                val datosAnuales = agruparPorAnioConRegistros(habito.listaFechas, habito.listaValores)
                xValues = datosAnuales.keys.toMutableList()
                yValues = datosAnuales.values.map { it.toString() }.toMutableList()
                barras = 5f

                fechaTexto = if (xValues.isNotEmpty()) {
                    xValues.last()
                } else {
                    context.getString(R.string.sin_datos)
                }
            }
            else -> return@launch
        }

        withContext(Dispatchers.Main) {
            binding.textoFechaLineChart.text = fechaTexto
            cargarGraficoDeLineas(
                context,
                xValues,
                yValues,
                tiempoTraducido,
                tiempoInterno,
                formatoFechaArriba,
                barras,
                espacio,
                binding,
                habito,
                formatoFechaOriginal
            )
            binding.lineChart.resetViewPortOffsets()
        }
    }
}

private fun cargarGraficoDeLineas(
    context: Context,
    xValues: MutableList<String>,
    yValues: MutableList<String>,
    tiempoTraducido: String,
    tiempoInterno: String,
    formatoFechaArriba: String,
    barras: Float,
    espacio: Float,
    binding: FragmentEstadisticasBinding,
    habito: Habito,
    formatoFechaOriginal: SimpleDateFormat
) {
    val lineChart = binding.lineChart
    val textoMesAnio = binding.textoFechaLineChart
    val dateFormatOutputMesFECHA = SimpleDateFormat(formatoFechaArriba, Locale.getDefault())

    val entries = yValues.mapIndexed { index, value ->
        try {
            Entry(index.toFloat(), value.toFloat())
        } catch (e: Exception) {
            Log.e("ERROR", "Error al convertir valor en índice $index: $value", e)
            Entry(index.toFloat(), 0f)
        }
    }

    var unidad = habito.unidad.toString().take(5).lowercase().replaceFirstChar { it.uppercase() }
    if (unidad == "Null") {
        unidad = context.getString(R.string.unidades_checks)
    }

    val dataSet = LineDataSet(entries, "$unidad x $tiempoTraducido").apply {
        color = habito.colorHabito
        setCircleColor(ContextCompat.getColor(context, R.color.tittle_color))
        circleRadius = 5f
        setDrawCircleHole(false)
        setDrawValues(true)
        valueTextSize = context.resources.getDimension(R.dimen.t_graficos_estadis)
        valueTextColor = ContextCompat.getColor(context, R.color.tittle_color)
        valueTypeface = Typeface.DEFAULT_BOLD
        enableDashedLine(10f, 5f, 0f)
        setDrawFilled(true)
        fillDrawable = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(habito.colorHabito, Color.TRANSPARENT)
        )
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return formatearNumero(value)
            }
        }
    }

    val lineData = LineData(dataSet)
    lineChart.data = lineData
    lineChart.invalidate()

    val fechaXAxis = if(tiempoInterno == "Semana"){
        IndexAxisValueFormatter(xValues.map { it.split(" ")[0] })
    }else{
        IndexAxisValueFormatter(xValues.map { it.split("@")[0] })
    }

    lineChart.xAxis.apply {
        valueFormatter = fechaXAxis
        position = XAxis.XAxisPosition.BOTTOM
        setDrawGridLines(false)
        granularity = 1f
        textSize = context.resources.getDimension(R.dimen.t_graficos_estadis)
        textColor = ContextCompat.getColor(context, R.color.tittle_color)
        axisLineColor = ContextCompat.getColor(context, R.color.tittle_color)
        axisLineWidth = 1.5f
        spaceMax = espacio
        spaceMin = espacio
    }

    if (tiempoInterno == "Día" && !habito.tipoNumerico) {
        lineChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 1.1f
            granularity = 1f
            labelCount = 2
            textSize = context.resources.getDimension(R.dimen.t_graficos_estadis)
            textColor = ContextCompat.getColor(context, R.color.tittle_color)
            axisLineColor = ContextCompat.getColor(context, R.color.tittle_color)
            axisLineWidth = 1.5f
            gridColor = Color.argb(50, 255, 255, 255)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return when (value) {
                        0f -> "0"
                        1f -> "1"
                        else -> ""
                    }
                }
            }
        }
    } else {
        lineChart.axisLeft.apply {
            resetAxisMaximum()
            axisMinimum = 0f
            textSize = context.resources.getDimension(R.dimen.t_graficos_estadis)
            textColor = ContextCompat.getColor(context, R.color.tittle_color)
            axisLineColor = ContextCompat.getColor(context, R.color.tittle_color)
            axisLineWidth = 1.5f
            granularity = 0f
            labelCount = 6
            gridColor = ContextCompat.getColor(context, R.color.tittle_color_50)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return formatearNumero(value)
                }
            }
        }
    }

    lineChart.axisRight.isEnabled = false

    lineChart.legend.apply {
        textSize = context.resources.getDimension(R.dimen.t_graficos_estadis)
        textColor = ContextCompat.getColor(context, R.color.tittle_color)
        typeface = Typeface.DEFAULT_BOLD
    }

    lineChart.description.isEnabled = false
    lineChart.setVisibleXRangeMaximum(barras)
    lineChart.setVisibleXRangeMinimum(barras)
    lineChart.isDragEnabled = true
    lineChart.setScaleEnabled(false)

    lineChart.moveViewToX(lineData.xMax)

    fun actualizarFechaTexto() {
        val visibleXIndex = lineChart.highestVisibleX.toInt().coerceAtLeast(0).coerceAtMost(xValues.size - 1)
        textoMesAnio.text = when (tiempoInterno) {
            "Día" -> {
                val newDate = formatoFechaOriginal.parse(habito.listaFechas[visibleXIndex])
                dateFormatOutputMesFECHA.format(newDate!!).uppercase()
            }
            "Semana" -> {
                xValues[visibleXIndex].split(" ").last().uppercase().replace("@", " ")
            }
            "Mes" -> {
                xValues[visibleXIndex].split("@")[1]
            }
            else -> ""
        }
    }

    lineChart.onChartGestureListener = object : OnChartGestureListener {
        override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
            actualizarFechaTexto()
        }
        override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
        override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
        override fun onChartLongPressed(me: MotionEvent?) {}
        override fun onChartDoubleTapped(me: MotionEvent?) {}
        override fun onChartSingleTapped(me: MotionEvent?) {}
        override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}
        override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
    }

    lineChart.notifyDataSetChanged()
    lineChart.invalidate()
}