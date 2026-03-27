package com.aluma.laundry.ui.view.screens

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocalLaundryService
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aluma.laundry.R
import com.aluma.laundry.bluetooth.BluetoothHelper
import com.aluma.laundry.bluetooth.BluetoothSender
import com.aluma.laundry.data.attendance.remote.AttendanceRemoteViewModel
import com.aluma.laundry.data.employee.remote.EmployeeRemoteViewModel
import com.aluma.laundry.data.logmachine.local.LogMachineLocalViewModel
import com.aluma.laundry.data.logmachine.model.LogMachineLocal
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.machine.remote.MachineRemoteViewModel
import com.aluma.laundry.data.order.local.OrderLocalViewModel
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.utils.SyncStatus
import com.aluma.laundry.data.service.remote.ServiceRemoteViewModel
import com.aluma.laundry.data.store.StoreRemoteViewModel
import com.aluma.laundry.ui.view.components.EmptyState
import com.aluma.laundry.ui.view.components.bottomsheet.AttendanceBottomSheet
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheet
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformation
import com.aluma.laundry.ui.view.components.bottomsheet.OrderBottomSheetInformationTime
import com.aluma.laundry.ui.view.components.itemscard.ItemOrderCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@SuppressLint("MissingPermission", "LocalContextGetResourceValueCall", "StringFormatMatches")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenHome(
    storeRemoteViewModel: StoreRemoteViewModel = koinInject(),
    machineLocalViewModel: MachineLocalViewModel = koinInject(),
    orderLocalViewModel: OrderLocalViewModel = koinInject(),
    machineRemoteViewModel: MachineRemoteViewModel = koinInject(),
    serviceRemoteViewModel: ServiceRemoteViewModel = koinInject(),
    logMachineLocalViewModel: LogMachineLocalViewModel = koinInject(),
    employeeRemoteViewModel: EmployeeRemoteViewModel = koinInject(),
    attendanceRemoteViewModel: AttendanceRemoteViewModel = koinInject(),
    bluetoothHelper: BluetoothHelper,
    onNavigateMachine: () -> Unit,
    onNavigateOrder: () -> Unit,
    onNavigateSettings: () -> Unit
) {
    val nameStore by storeRemoteViewModel.nameStore.collectAsState()
    val orders by orderLocalViewModel.ordersFilter.collectAsState()
    val machines by machineLocalViewModel.machines.collectAsState()
    val selectedOrder by orderLocalViewModel.selectedOrder.collectAsState()

    // Employee & Attendance states
    val employees by employeeRemoteViewModel.employees.collectAsState()
    val employeeId by employeeRemoteViewModel.employeeId.collectAsState()
    val employeeName by employeeRemoteViewModel.employeeName.collectAsState()
    val isCheckedIn by attendanceRemoteViewModel.isCheckedIn.collectAsState()
    val todayAttendance by attendanceRemoteViewModel.todayAttendance.collectAsState()
    val attendanceLoading by attendanceRemoteViewModel.isLoading.collectAsState()

    var showOrderSheet by remember { mutableStateOf(false) }
    var showOrderSheetMachine by remember { mutableStateOf(false) }
    var showOrderSheetMachineRunning by remember { mutableStateOf(false) }
    var showEmployeeDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Fetch today's attendance when employee is already selected
    LaunchedEffect(employeeId) {
        if (!employeeId.isNullOrEmpty()) {
            attendanceRemoteViewModel.fetchTodayAttendance(employeeId!!)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            employeeRemoteViewModel.fetchEmployees()
                            showEmployeeDialog = true
                        }
                    ) {
                        Text(
                            text = nameStore ?: stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (!employeeName.isNullOrEmpty()) {
                                    "• $employeeName •"
                                } else {
                                    stringResource(id = R.string.select_employee)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (!employeeName.isNullOrEmpty()) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(id = R.string.settings), tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
        // floatingActionButton sengaja dikosongkan agar kita bisa mengontrol posisi custom FAB secara bebas
    ) { innerPadding ->

        // --- CONTAINER UTAMA ---
        // Box ini penting agar background FAB bisa Full Screen murni
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. KONTEN DATA (List & Ringkasan)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Ringkasan Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusMiniCard(
                        label = stringResource(id = R.string.order_count),
                        value = orders.size.toString(),
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary,
                        onClick = onNavigateOrder
                    )
                    StatusMiniCard(
                        label = stringResource(id = R.string.machines_running),
                        value = machines.count { it.inUse }.toString(),
                        icon = Icons.Default.LocalLaundryService,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF4CAF50),
                        onClick = onNavigateMachine
                    )
                }

                // Daftar Pesanan
                if (orders.isEmpty()) {
                    EmptyState(
                        title = stringResource(id = R.string.no_orders_yet),
                        message = stringResource(id = R.string.no_orders_message),
                        icon = Icons.AutoMirrored.Filled.ReceiptLong
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = stringResource(id = R.string.active_queue),
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        items(orders, key = { it.id }) { order ->
                            ItemOrderCard(
                                order = order,
                                onSelect = {
                                    orderLocalViewModel.setSelectedOrder(order)
                                    if (order.stepMachine < 2) {
                                        machineLocalViewModel.filterMachine(
                                            type = order.typeMachineService,
                                            size = order.sizeMachine,
                                            stepMachine = order.stepMachine
                                        )
                                        showOrderSheetMachine = true
                                    } else {
                                        showOrderSheetMachineRunning = true
                                    }
                                }
                            )
                        }
                        // Ruang kosong agar item terakhir tidak tertimpa FAB
                        item { Spacer(modifier = Modifier.height(88.dp)) }
                    }
                }
            }

            // 2. Simple FAB (Extended)
            ExtendedFloatingActionButton(
                onClick = {
                    if (employeeId.isNullOrEmpty()) {
                        Toast.makeText(context, context.getString(R.string.select_employee_first), Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    }
                    if (!isCheckedIn) {
                        Toast.makeText(context, context.getString(R.string.check_in_first), Toast.LENGTH_SHORT).show()
                        return@ExtendedFloatingActionButton
                    }
                    machineRemoteViewModel.fetchMachine()
                    serviceRemoteViewModel.fetchServices()
                    showOrderSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.new_order),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 16.dp, end = 16.dp)
                    .navigationBarsPadding()
            )
        }
    }

    // --- BOTTOM SHEETS LOGIC ---
    if (showOrderSheet) {
        OrderBottomSheet(
            onDismissRequest = { showOrderSheet = false },
            onSubmit = { orderLocalViewModel.addOrder(orderLocal = it) },
            adminEmployeeId = employeeId
        )
    }

    if (showOrderSheetMachine && selectedOrder != null) {
        OrderBottomSheetInformation(
            order = selectedOrder!!,
            onDismissRequest = { showOrderSheetMachine = false },
            onSubmit = { machine, onDone ->
                if (machine != null) {
                    bluetoothHelper.requestBluetooth {
                        scope.launch(Dispatchers.IO) {
                            val sender = BluetoothSender()
                            val success = sender.sendToESP(
                                context = context,
                                address = machine.bluetoothAddress.orEmpty(),
                                message = "m:${machine.numberMachine},t:${machine.timer},s:true"
                            )

                            withContext(Dispatchers.Main) {
                                if (success) {
                                    processMachineActivation(
                                        order = selectedOrder!!,
                                        machine = machine,
                                        orderLocalViewModel = orderLocalViewModel,
                                        machineLocalViewModel = machineLocalViewModel,
                                        logMachineLocalViewModel = logMachineLocalViewModel
                                    )
                                    Toast.makeText(context, context.getString(R.string.machine_active_toast, machine.numberMachine), Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, context.getString(R.string.bluetooth_failed), Toast.LENGTH_SHORT).show()
                                }
                                onDone()
                            }
                        }
                    }
                }
            }
        )
    }

    if (showOrderSheetMachineRunning && selectedOrder != null) {
        machines.find { it.numberMachine == selectedOrder!!.numberMachine }?.let { machine ->
            OrderBottomSheetInformationTime(
                order = selectedOrder!!,
                machine = machine,
                stepMachine = selectedOrder!!.stepMachine,
                onDismissRequest = { showOrderSheetMachineRunning = false },
                machineNumber = selectedOrder!!.numberMachine
            )
        }
    }

    // --- ATTENDANCE BOTTOM SHEET ---
    if (showEmployeeDialog) {
        AttendanceBottomSheet(
            employees = employees,
            currentEmployeeId = employeeId,
            employeeName = employeeName,
            todayAttendance = todayAttendance,
            isCheckedIn = isCheckedIn,
            isLoading = attendanceLoading,
            onDismissRequest = { showEmployeeDialog = false },
            onSelectEmployee = { employee ->
                employeeRemoteViewModel.selectEmployee(employee)
            },
            onCheckIn = {
                if (!employeeId.isNullOrEmpty()) {
                    attendanceRemoteViewModel.checkIn(
                        employeeId = employeeId!!,
                        onSuccess = {
                            Toast.makeText(context, context.getString(R.string.check_in_success), Toast.LENGTH_SHORT).show()
                            showEmployeeDialog = false
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            },
            onCheckOut = {
                attendanceRemoteViewModel.checkOut(
                    onSuccess = {
                        Toast.makeText(context, context.getString(R.string.check_out_success), Toast.LENGTH_SHORT).show()
                        showEmployeeDialog = false
                    },
                    onError = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onClearEmployee = {
                employeeRemoteViewModel.clearEmployee()
                attendanceRemoteViewModel.resetAttendance()
            }
        )
    }
}

// --- HELPER COMPONENT ---
@Composable
fun StatusMiniCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 1.dp,
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F3F4))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Icon (Subtle)
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .graphicsLayer {
                        alpha = 0.05f
                    },
                tint = color
            )

            // Accent Bar on the left
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(90.dp)
                    .background(color)
                    .align(Alignment.CenterStart)
            )

            Column(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2D3142)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(id = R.string.click_to_view_details),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = color
                    )
                }
            }
        }
    }
}

// --- HELPER FUNCTION (Logic) ---
private fun processMachineActivation(
    order: OrderLocal,
    machine: MachineLocal,
    orderLocalViewModel: OrderLocalViewModel,
    machineLocalViewModel: MachineLocalViewModel,
    logMachineLocalViewModel: LogMachineLocalViewModel
) {
    val now = Instant.now()
    val formattedDate = DateTimeFormatter
        .ofPattern("yyyy-MM-dd HH:mm:ss.SSSX")
        .withZone(ZoneOffset.UTC)
        .format(now)

    // 1. Update Order
    orderLocalViewModel.updateOrder(order.copy(
        stepMachine = order.stepMachine + 2,
        numberMachine = machine.numberMachine
    ))

    // 2. Update Machine
    machineLocalViewModel.updateMachine(machine.copy(
        inUse = true,
        order = order.id,
        timeOn = formattedDate
    ))

    // 3. Add Log
    logMachineLocalViewModel.addLogMachine(LogMachineLocal(
        numberMachine = machine.numberMachine,
        sizeMachine = machine.sizeMachine,
        typeMachine = machine.typeMachine,
        user = machine.user,
        store = machine.store,
        date = formattedDate,
        syncStatus = SyncStatus.PENDING
    ))
}