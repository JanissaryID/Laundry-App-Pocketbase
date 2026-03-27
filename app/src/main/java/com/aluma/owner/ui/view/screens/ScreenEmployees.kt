package com.aluma.owner.ui.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aluma.owner.R
import com.aluma.owner.data.employee.remote.EmployeeRemoteViewModel
import com.aluma.owner.data.realtime.RealtimeViewModel
import com.aluma.owner.ui.view.components.EmptyState
import com.aluma.owner.ui.view.components.bottomsheet.EmployeeBottomSheet
import com.aluma.owner.ui.view.components.itemscard.ItemEmployeeCard
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenEmployees(
    employeeRemoteViewModel: EmployeeRemoteViewModel = koinInject(),
    realtimeViewModel: RealtimeViewModel = koinInject(),
    onBack: () -> Unit,
    onAttendance: (String, String) -> Unit
) {
    val employees by employeeRemoteViewModel.employees.collectAsState()
    val employeesSelected by employeeRemoteViewModel.selectedEmployee.collectAsState()
    val attendanceCounts by employeeRemoteViewModel.attendanceCounts.collectAsState()

    var showBottomSheet by remember { mutableStateOf(false) }
    var editOrAdd by remember { mutableStateOf(false) }

    // Realtime SSE: re-fetch employees when server-side events arrive
    LaunchedEffect(Unit) {
        realtimeViewModel.realtimeEvent.collect { collectionName ->
            if (collectionName == "LaundryEmployee") {
                employeeRemoteViewModel.fetchEmployees()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.employee_list_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.employee_list_subtitle), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    employeeRemoteViewModel.setSelectedEmployee(null)
                    editOrAdd = true
                    showBottomSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.employee_list_add_button)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (employees.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        title = stringResource(R.string.employee_list_empty_title),
                        message = stringResource(R.string.employee_list_empty_message)
                    )
                }
            } else {
                var deletingId by remember { mutableStateOf<String?>(null) }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(employees) { employee ->
                        ItemEmployeeCard(
                            employee = employee,
                            attendanceCount = attendanceCounts[employee.id] ?: 0,
                            onClick = {
                                onAttendance(employee.id.orEmpty(), employee.name.orEmpty())
                            },
                            onEdit = {
                                employeeRemoteViewModel.setSelectedEmployee(employee)
                                editOrAdd = false
                                showBottomSheet = true
                            },
                            onDelete = {
                                if (deletingId == null) {
                                    deletingId = employee.id
                                    employeeRemoteViewModel.deleteEmployee(
                                        employeeId = employee.id.orEmpty()
                                    ) { success ->
                                        deletingId = null
                                    }
                                }
                            },
                            isDeleting = deletingId == employee.id
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showBottomSheet) {
        EmployeeBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            onSubmit = { name ->
                showBottomSheet = false
                if (editOrAdd) {
                    employeeRemoteViewModel.addEmployee(name) { }
                } else {
                    employeeRemoteViewModel.editEmployee(
                        employeeId = employeesSelected!!.id!!,
                        name = name
                    ) { }
                }
            },
        )
    }
}
