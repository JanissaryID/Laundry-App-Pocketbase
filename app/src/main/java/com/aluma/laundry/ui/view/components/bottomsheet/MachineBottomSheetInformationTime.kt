package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.ui.view.components.CountdownTimer
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineBottomSheetInformationTime(
    machine: MachineLocal,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val machineType = if (machine.typeMachine) "Mesin Pengering" to Icons.Default.LocalFireDepartment
    else "Mesin Cuci" to Icons.Default.WaterDrop
    val machineSize = if (machine.sizeMachine) "BESAR" else "KECIL"

    val (startTimeMillis, endTimeMillis) = remember(machine.timeOn, machine.timer) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX").withZone(ZoneOffset.UTC)
        try {
            val start = formatter.parse(machine.timeOn, Instant::from)
            val end = start.plus(Duration.ofMinutes(machine.timer.toLong()))
            start.toEpochMilli() to end.toEpochMilli()
        } catch (_: Exception) {
            val now = System.currentTimeMillis()
            now to now
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 🔹 Header Mesin + Ukuran
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mesin #${machine.numberMachine}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = machineSize,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // 🔹 Icon dan Tipe Mesin
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = machineType.second,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = machineType.first,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // 🔹 Countdown Timer
            CountdownTimer(
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis,
                onFinish = {},
                modifier = Modifier.fillMaxWidth()
            )

            // 🔹 Tombol Tutup
            Button(
                onClick = onDismissRequest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Tutup")
            }
        }
    }
}


