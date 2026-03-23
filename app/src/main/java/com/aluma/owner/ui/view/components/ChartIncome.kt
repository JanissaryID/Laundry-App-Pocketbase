package com.aluma.owner.ui.view.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val labelColor = onSurfaceColor.copy(alpha = 0.6f)

    // Data points processing
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
            Offset(day.toFloat(), (incomePerDay[day] ?: 0L).toFloat())
        }
    }

    if (pointsData.isEmpty()) return

    val maxVal = remember(pointsData) { 
        val m = pointsData.maxOfOrNull { it.y } ?: 0f
        if (m <= 0f) 50000f else m * 1.2f // Add some headroom
    }

    // Animation state
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(pointsData) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
    }

    // Interactivity state
    var selectedPointIndex by remember { mutableStateOf(-1) }
    var touchX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(top = 16.dp, bottom = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(pointsData) {
                    detectTapGestures { offset ->
                        val width = size.width
                        val xStep = width / (daysInMonth - 1)
                        val index = (offset.x / xStep).toInt().coerceIn(0, pointsData.size - 1)
                        selectedPointIndex = index
                        touchX = offset.x
                    }
                }
                .pointerInput(pointsData) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val width = size.width
                            val xStep = width / (daysInMonth - 1)
                            val index = (offset.x / xStep).toInt().coerceIn(0, pointsData.size - 1)
                            selectedPointIndex = index
                            touchX = offset.x
                        },
                        onDragEnd = { selectedPointIndex = -1 },
                        onDragCancel = { selectedPointIndex = -1 }
                    ) { change, _ ->
                        val width = size.width
                        val xStep = width / (daysInMonth - 1)
                        val index = (change.position.x / xStep).toInt().coerceIn(0, pointsData.size - 1)
                        selectedPointIndex = index
                        touchX = change.position.x
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val xStep = width / (daysInMonth - 1)

            // Drawing X-Axis labels
            val androidPaint = android.graphics.Paint().apply {
                color = labelColor.toArgb()
                textSize = 10.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            val importantDays = listOf(1, 5, 10, 15, 20, 25, daysInMonth)
            importantDays.forEach { day ->
                val x = (day - 1) * xStep
                drawContext.canvas.nativeCanvas.drawText(
                    day.toString(),
                    x,
                    height,
                    androidPaint
                )
            }

            // Prepare Path
            val path = Path()
            val fillPath = Path()
            
            if (pointsData.isNotEmpty()) {
                val firstPoint = pointsData[0]
                val startX = 0f
                val startY = height - (firstPoint.y / maxVal) * height * animationProgress.value
                
                path.moveTo(startX, startY)
                fillPath.moveTo(startX, height)
                fillPath.lineTo(startX, startY)

                for (i in 0 until pointsData.size - 1) {
                    val p1 = pointsData[i]
                    val p2 = pointsData[i + 1]

                    val x1 = i * xStep
                    val y1 = height - (p1.y / maxVal) * height * animationProgress.value
                    
                    val x2 = (i + 1) * xStep
                    val y2 = height - (p2.y / maxVal) * height * animationProgress.value

                    // Cubic Bezier for smooth curves
                    val controlX1 = x1 + (x2 - x1) / 2
                    val controlX2 = x1 + (x2 - x1) / 2
                    
                    path.cubicTo(controlX1, y1, controlX2, y2, x2, y2)
                    fillPath.cubicTo(controlX1, y1, controlX2, y2, x2, y2)
                }
                
                fillPath.lineTo(width, height)
                fillPath.close()

                // Draw fill
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw selection indicator
                if (selectedPointIndex != -1) {
                    val p = pointsData[selectedPointIndex]
                    val x = selectedPointIndex * xStep
                    val y = height - (p.y / maxVal) * height * animationProgress.value
                    
                    // Vertical guideline
                    drawLine(
                        color = primaryColor.copy(alpha = 0.2f),
                        start = Offset(x, 0f),
                        end = Offset(x, height),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    // Highlight point
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        // TOOLTIP OVERLAY
        if (selectedPointIndex != -1) {
            val p = pointsData[selectedPointIndex]
            val amount = p.y.toLong()
            val day = p.x.toInt()
            
            // Positioning the tooltip
            val tooltipWidth = 140.dp
            val xOffset = remember(selectedPointIndex) {
                 ((selectedPointIndex.toFloat() / (daysInMonth - 1)) * 300).dp // Estimate for position
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(if (selectedPointIndex > daysInMonth / 2) Alignment.TopStart else Alignment.TopEnd)
                        .padding(16.dp)
                        .sizeIn(minWidth = 100.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Tgl $day ${today.month.name.lowercase().capitalize(Locale.getDefault())}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = amount.formatToRupiah(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}