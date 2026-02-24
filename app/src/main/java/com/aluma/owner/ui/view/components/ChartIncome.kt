package com.aluma.owner.ui.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.extensions.formatToSinglePrecision
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*
import com.aluma.owner.utils.formatToRupiah
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ChartIncome(
    storeId: String,
    incomeList: List<Triple<String, String, String>>
) {
    val today = LocalDate.now()
    val daysInMonth = YearMonth.of(today.year, today.month).lengthOfMonth()

    val pointsData = remember(storeId, incomeList) {
        val incomePerDay = mutableMapOf<Int, Long>()
        incomeList.filter { it.first == storeId }.forEach { (_, dateStr, totalStr) ->
            val parsedDate = try {
                LocalDate.parse(dateStr.substring(0, 10))
            } catch (_: Exception) {
                null
            }

            if (parsedDate != null &&
                parsedDate.month == today.month &&
                parsedDate.year == today.year
            ) {
                val day = parsedDate.dayOfMonth
                incomePerDay[day] = incomePerDay.getOrDefault(day, 0L) + (totalStr.toLongOrNull() ?: 0L)
            }
        }
        (1..daysInMonth).map { day ->
            Point(day.toFloat(), (incomePerDay[day] ?: 0L).toFloat())
        }
    }

    if (pointsData.isEmpty()) return

    val rawMaxIncome = pointsData.maxOf { it.y }
    val steps = 5
    
    // Calculate a "nice" max income for better scaling
    val niceMaxIncome = remember(rawMaxIncome) {
        if (rawMaxIncome <= 0f) 50000f
        else {
            val exponent = Math.floor(Math.log10(rawMaxIncome.toDouble())).toInt()
            val fraction = rawMaxIncome / Math.pow(10.0, exponent.toDouble()).toFloat()
            val niceFraction = when {
                fraction <= 1.0f -> 1.0f
                fraction <= 2.0f -> 2.0f
                fraction <= 5.0f -> 5.0f
                else -> 10.0f
            }
            niceFraction * Math.pow(10.0, exponent.toDouble()).toFloat()
        }
    }

    val xAxisData = AxisData.Builder()
        .axisStepSize(45.dp) // Slightly wider for better spacing
        .backgroundColor(Color.Transparent)
        .steps(pointsData.size - 1)
        .labelData { i -> 
            val day = i + 1
            // Show labels for 1st, 5th, 10th, 15th, 20th, 25th, and last day to avoid crowding
            if (day == 1 || day % 5 == 0 || day == daysInMonth) day.toString() else ""
        }
        .labelAndAxisLinePadding(15.dp)
        .axisLabelColor(MaterialTheme.colorScheme.onSurface)
        .axisLineColor(MaterialTheme.colorScheme.outline)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(steps)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i ->
            val value = (i * niceMaxIncome / steps).toLong()
            when {
                value >= 1_000_000 -> "Rp ${(value / 1_000_000f).formatToSinglePrecision()} Jt"
                value >= 1_000 -> "Rp ${value / 1_000} Rb"
                else -> "Rp $value"
            }
        }
        .axisLabelColor(MaterialTheme.colorScheme.onSurface)
        .axisLineColor(MaterialTheme.colorScheme.outline)
        .build()

    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault()) }

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    lineStyle = LineStyle(
                        color = MaterialTheme.colorScheme.primary,
                        lineType = LineType.SmoothCurve(isDotted = false)
                    ),
                    intersectionPoint = IntersectionPoint(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    selectionHighlightPoint = SelectionHighlightPoint(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    shadowUnderLine = ShadowUnderLine(
                        alpha = 0.5f,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                Color.Transparent
                            )
                        )
                    ),
                    selectionHighlightPopUp = SelectionHighlightPopUp(
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        popUpLabel = { x, y ->
                            val day = x.toInt()
                            val amount = y.toLong()
                            val fullDate = try { today.withDayOfMonth(day) } catch (_: Exception) { today }.format(dateFormatter)
                            "$fullDate\n${amount.formatToRupiah()}"
                        }
                    )
                )
            )
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(color = MaterialTheme.colorScheme.outlineVariant),
        backgroundColor = MaterialTheme.colorScheme.surface
    )

    LineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        lineChartData = lineChartData
    )
}