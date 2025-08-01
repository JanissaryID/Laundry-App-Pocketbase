package com.aluma.laundry.ui.view.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun ChartIncome(
    storeId: String, // id store yang sedang ditampilkan
    incomeList: List<Triple<String, String, String>>
) {
    val today = LocalDate.now()
    val currentMonth = today.month
    val currentYear = today.year
    val daysInMonth = YearMonth.of(currentYear, currentMonth).lengthOfMonth()

    // 🧠 Map tanggal ke total (default 0)
    val dailyIncomeMap = remember(storeId, incomeList) {
        val incomePerDay = MutableList(daysInMonth) { index -> (index + 1) to 0L }

        incomeList
            .filter { it.first == storeId }
            .forEach { (_, dateStr, totalStr) ->
                val parsedDate = try {
                    LocalDate.parse(dateStr.substring(0, 10)) // YYYY-MM-DD
                } catch (_: Exception) {
                    null
                }

                if (parsedDate != null && parsedDate.month == currentMonth && parsedDate.year == currentYear) {
                    val dayIndex = parsedDate.dayOfMonth - 1
                    val total = totalStr.toLongOrNull() ?: 0L
                    incomePerDay[dayIndex] = parsedDate.dayOfMonth to total
                }
            }

        incomePerDay
    }

    val startAxis = rememberStartAxis(
        valueFormatter = { value, _ -> "Rp ${value.toInt() / 1000}K" }
    )

    val bottomAxis = rememberBottomAxis(
        valueFormatter = { value, _ -> value.toInt().toString() }
    )

    Chart(
        chart = lineChart(),
        model = entryModelOf(*dailyIncomeMap.toTypedArray()),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .padding(horizontal = 16.dp),
        startAxis = startAxis,
        bottomAxis = bottomAxis
    )
}