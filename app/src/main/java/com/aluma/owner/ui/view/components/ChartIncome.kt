package com.aluma.owner.ui.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun ChartIncome(
    storeId: String,
    incomeList: List<Triple<String, String, String>>
) {
    val today = LocalDate.now()
    val daysInMonth = YearMonth.of(today.year, today.month).lengthOfMonth()

    val dailyIncomeMap = remember(storeId, incomeList) {
        val incomePerDay = MutableList(daysInMonth) { index -> (index + 1) to 0L }
        incomeList.filter { it.first == storeId }.forEach { (_, dateStr, totalStr) ->
            val parsedDate = try { LocalDate.parse(dateStr.substring(0, 10)) } catch (_: Exception) { null }
            if (parsedDate != null && parsedDate.month == today.month && parsedDate.year == today.year) {
                incomePerDay[parsedDate.dayOfMonth - 1] = parsedDate.dayOfMonth to (totalStr.toLongOrNull() ?: 0L)
            }
        }
        incomePerDay
    }

    val startAxis = rememberStartAxis(
        valueFormatter = { value, _ ->
            if (value >= 1000000) "${String.format("%.1f", value / 1000000f)}jt"
            else "${value.toInt() / 1000}rb"
        }
    )

    val bottomAxis = rememberBottomAxis(
        valueFormatter = { value, _ -> value.toInt().toString() }
    )

    Chart(
        chart = lineChart(
            lines = listOf(
                lineSpec(
                    lineColor = MaterialTheme.colorScheme.primary,
                    lineThickness = 3.dp,
                    // Efek gradasi di bawah garis (membuat UI jauh lebih modern)
                    lineBackgroundShader = verticalGradient(
                        colors = arrayOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                        )
                    )
                )
            )
        ),
        model = entryModelOf(*dailyIncomeMap.toTypedArray()),
        modifier = Modifier.fillMaxWidth().height(220.dp),
        startAxis = startAxis,
        bottomAxis = bottomAxis
    )
}