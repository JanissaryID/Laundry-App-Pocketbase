package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.R
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.utils.Quad
import com.aluma.laundry.ui.view.components.CountdownTimer
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineBottomSheetInformationTime(
    machine: MachineLocal,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Logika Tipe Mesin (Warna & Ikon yang berbeda)
    val (machineLabel, machineIcon, accentColor) = if (machine.typeMachine) {
        Triple(stringResource(id = R.string.machine_dryer), Icons.Default.LocalFireDepartment, Color(0xFFEF6C00)) // Oranye
    } else {
        Triple(stringResource(id = R.string.machine_washer), Icons.Default.WaterDrop, Color(0xFF2196F3)) // Biru
    }

    val machineSize = if (machine.sizeMachine) stringResource(id = R.string.capacity_large_upper) else stringResource(id = R.string.capacity_small_upper)

    // Perhitungan Waktu
    val (startTimeStr, endTimeStr, startTimeMillis, endTimeMillis) = remember(machine.timeOn, machine.timer) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX").withZone(ZoneOffset.UTC)
        val timeDisplayFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

        try {
            val start = formatter.parse(machine.timeOn, Instant::from)
            val end = start.plus(Duration.ofMinutes(machine.timer.toLong()))

            val startStr = timeDisplayFormatter.format(start)
            val endStr = timeDisplayFormatter.format(end)

            Quad(startStr, endStr, start.toEpochMilli(), end.toEpochMilli())
        } catch (_: Exception) {
            Quad("--:--", "--:--", System.currentTimeMillis(), System.currentTimeMillis())
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 🔹 HEADER (Modern)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = machineIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.machine_unit_number, machine.numberMachine),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1E1E)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = machineLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = accentColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    text = machineSize,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }
            }

            // 🔹 VISUAL TIMER CARD (Premium)
            Surface(
                color = accentColor.copy(alpha = 0.05f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.time_left),
                        style = MaterialTheme.typography.labelLarge,
                        color = accentColor.copy(alpha = 0.7f),
                        fontWeight = FontWeight.SemiBold
                    )

                    CountdownTimer(
                        startTimeMillis = startTimeMillis,
                        endTimeMillis = endTimeMillis,
                        onFinish = { /* Finish Logic */ },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeInfoBlock(label = stringResource(id = R.string.start), time = startTimeStr)
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        TimeInfoBlock(label = stringResource(id = R.string.estimated_finish), time = endTimeStr, highlightColor = accentColor)
                    }
                }
            }

            // 🔹 FOOTER ACTION
            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(stringResource(id = R.string.finish_monitoring), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
fun TimeInfoBlock(label: String, time: String, highlightColor: Color = Color.Gray) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(text = time, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = highlightColor)
    }
}