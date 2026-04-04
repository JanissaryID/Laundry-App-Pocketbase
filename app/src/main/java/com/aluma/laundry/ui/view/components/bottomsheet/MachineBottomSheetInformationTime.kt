package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.draw.clip
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

    // Dryer = warm palette, Washer = cool palette
    val isDryer = machine.typeMachine

    val machineLabel = if (isDryer) stringResource(id = R.string.machine_dryer) else stringResource(id = R.string.machine_washer)
    val machineIcon = if (isDryer) Icons.Default.LocalFireDepartment else Icons.Default.WaterDrop
    val machineSize = if (machine.sizeMachine) stringResource(id = R.string.capacity_12kg) else stringResource(id = R.string.capacity_7kg)

    // Color per machine type
    val accentColor = if (isDryer) Color(0xFFFF6B35) else Color(0xFF1E88E5)

    val (startTimeStr, endTimeStr, startTimeMillis, endTimeMillis) = remember(machine.timeOn, machine.timer) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX").withZone(ZoneOffset.UTC)
        val timeDisplayFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

        try {
            val start = formatter.parse(machine.timeOn, Instant::from)
            val end = start.plus(Duration.ofMinutes(machine.timer.toLong()))
            Quad(
                timeDisplayFormatter.format(start),
                timeDisplayFormatter.format(end),
                start.toEpochMilli(),
                end.toEpochMilli()
            )
        } catch (_: Exception) {
            Quad("--:--", "--:--", System.currentTimeMillis(), System.currentTimeMillis())
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // ── Header (same style as ItemMachineCard) ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = accentColor.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = machineIcon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.machine_unit_format, machine.numberMachine),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = machineLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // Size badge
                Surface(
                    color = if (machine.sizeMachine) Color(0xFFF3E5F5) else Color(0xFFF5F5F5),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = machineSize,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (machine.sizeMachine) Color(0xFF7B1FA2) else Color(0xFF616161)
                    )
                }
            }

            // ── Body content ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp, bottom = 32.dp)
            ) {
                // Timer
                CountdownTimer(
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = endTimeMillis,
                    onFinish = { /* Finish Logic */ },
                    accentColor = accentColor
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Time info cards side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TimeInfoCard(
                        label = stringResource(id = R.string.start),
                        time = startTimeStr,
                        backgroundColor = accentColor.copy(alpha = 0.08f),
                        timeColor = Color(0xFF212121),
                        modifier = Modifier.weight(1f)
                    )
                    TimeInfoCard(
                        label = stringResource(id = R.string.estimated_finish),
                        time = endTimeStr,
                        backgroundColor = accentColor.copy(alpha = 0.15f),
                        timeColor = accentColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Action button
                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text(
                        text = stringResource(id = R.string.finish_monitoring),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeInfoCard(
    label: String,
    time: String,
    backgroundColor: Color,
    timeColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF9E9E9E)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = time,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = timeColor
            )
        }
    }
}