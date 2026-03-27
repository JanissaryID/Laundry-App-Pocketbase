package com.aluma.laundry.ui.view.components.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aluma.laundry.R
import com.aluma.laundry.ui.view.components.dialog.CheckoutConfirmDialog
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceBottomSheet(
    employees: List<com.aluma.laundry.data.employee.model.EmployeeRemote>,
    currentEmployeeId: String?,
    employeeName: String?,
    todayAttendance: com.aluma.laundry.data.attendance.model.AttendanceRemote?,
    isCheckedIn: Boolean,
    isLoading: Boolean,
    onDismissRequest: () -> Unit,
    onSelectEmployee: (com.aluma.laundry.data.employee.model.EmployeeRemote) -> Unit,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onClearEmployee: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showCheckOutDialog by remember { mutableStateOf(false) }
    
    val filteredEmployees = remember(searchQuery, employees) {
        if (searchQuery.isBlank()) {
            employees
        } else {
            employees.filter { it.name.orEmpty().contains(searchQuery, ignoreCase = true) }
        }
    }

    // Formatter for ISO timestamps if applicable, otherwise displaying raw
    val formatTime: (String?) -> String = { timeStr ->
        if (timeStr.isNullOrEmpty() || timeStr == "null") "-"
        else {
            try {
                // Try parsing PB ISO string: 2024-03-27 10:00:00.000Z
                val instant = Instant.parse(timeStr.replace(" ", "T"))
                val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                formatter.format(instant)
            } catch (e: Exception) {
                // If it fails, try a simpler fallback or just show the string
                if (timeStr.length > 5) timeStr.take(5) else timeStr
            }
        }
    }

    val formattedCheckIn = formatTime(todayAttendance?.cekIn)
    val formattedCheckOut = formatTime(todayAttendance?.cekOut)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.LightGray, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Text(
                text = "Attendance",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1C1E)
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Modern Searchable Dropdown
            Text(
                text = "Select Employee",
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery.ifBlank { employeeName.orEmpty() },
                    onValueChange = { 
                        searchQuery = it
                        expanded = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    placeholder = { Text("Search by name...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA)
                    ),
                    singleLine = true,
                    readOnly = false
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White).heightIn(max = 240.dp).clip(RoundedCornerShape(12.dp))
                ) {
                    if (filteredEmployees.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No employees found", color = Color.Gray) },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        filteredEmployees.forEach { employee ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = employee.name.orEmpty(),
                                        fontWeight = if (employee.id == currentEmployeeId) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSelectEmployee(employee)
                                    searchQuery = "" // Clear search after selection
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (employee.id == currentEmployeeId) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Status Card Below Dropdown (Animated Visibility)
            AnimatedVisibility(
                visible = !currentEmployeeId.isNullOrEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFBFBFB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                        // Header Row: Name (Left) and Clear Button (Right)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = employeeName.orEmpty(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3142)
                            )
                            IconButton(
                                onClick = onClearEmployee,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Selection",
                                    tint = Color.Gray
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // Check In and Check Out rows
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Login, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp).padding(end = 6.dp))
                            Text("Check In : ", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                            Text(
                                text = formattedCheckIn,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3142)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp).padding(end = 6.dp))
                            Text("Check Out : ", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                            Text(
                                text = formattedCheckOut,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D3142)
                            )
                        }

                        }
                    } // end surface

                    // Large Action Button
                    if (isLoading) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Button(
                            onClick = { 
                                if (isCheckedIn) {
                                    showCheckOutDialog = true
                                } else {
                                    onCheckIn()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCheckedIn) Color(0xFFFF5252) else Color(0xFF4CAF50)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCheckedIn) Icons.Default.Logout else Icons.Default.Login,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = if (isCheckedIn) stringResource(id = R.string.check_out)
                                    else stringResource(id = R.string.check_in),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
            
            // Helpful Hint if nothing selected
            if (currentEmployeeId.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.padding(20.dp),
                                tint = Color(0xFFBDBDBD)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Please select an employee to continue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        if (showCheckOutDialog) {
            CheckoutConfirmDialog(
                employeeName = employeeName.orEmpty(),
                onConfirm = {
                    showCheckOutDialog = false
                    onCheckOut()
                },
                onDismissRequest = { showCheckOutDialog = false }
            )
        }
    }
}
