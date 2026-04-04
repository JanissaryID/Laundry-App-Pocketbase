package com.aluma.laundry.data.order.local

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.income.remote.IncomeRemoteRepository
import com.aluma.laundry.data.logmachine.local.LogMachineLocalRepository
import com.aluma.laundry.data.logmachine.model.LogMachineLocal
import com.aluma.laundry.data.logmachine.model.LogMachineRemote
import com.aluma.laundry.data.logmachine.remote.LogMachineRemoteRepository
import com.aluma.laundry.data.machine.local.MachineLocalRepository
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.model.OrderRemote
import com.aluma.laundry.data.order.remote.OrderRemoteRepository
import com.aluma.laundry.workmanager.SyncUtils
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class OrderLocalViewModel(
    private val repo: OrderLocalRepository,
    private val machineRepo: MachineLocalRepository,
    private val orderRemoteRepository: OrderRemoteRepository,
    private val logMachineRemoteRepository: LogMachineRemoteRepository,
    private val logMachineLocalRepository: LogMachineLocalRepository,
    private val incomeRemoteRepository: IncomeRemoteRepository,
    private val client: PocketbaseClient,
    private val storePreferences: StorePreferences
) : ViewModel() {

    private val _maxStep = MutableStateFlow(4)
    private val _isLoggedIn = MutableStateFlow(false)

    private val _storeID = MutableStateFlow<String?>(null)
    private val _userID = MutableStateFlow<String?>(null)

    private val sharingStarted = SharingStarted.WhileSubscribed(5_000)

    val ordersFilter: StateFlow<List<OrderLocal>> = _maxStep
        .combine(repo.orders) { maxStep, allOrders ->
            allOrders.filter { it.stepMachine < maxStep }
                .reversed()
        }
        .stateIn(viewModelScope, sharingStarted, emptyList())

    val orders: StateFlow<List<OrderLocal>> = repo.orders
        .map { it.reversed() }
        .stateIn(viewModelScope, sharingStarted, emptyList())

    private val _selectedOrder = MutableStateFlow<OrderLocal?>(null)
    val selectedOrder: StateFlow<OrderLocal?> = _selectedOrder

    fun setSelectedOrder(order: OrderLocal?) {
        _selectedOrder.value = order
    }

    fun addOrder(orderLocal: OrderLocal) = viewModelScope.launch {
        repo.addOrder(orderLocal)
        SyncUtils.enqueueSync(storePreferences.context)
    }

    fun deleteOrder(orderLocal: OrderLocal) = viewModelScope.launch {
        repo.deleteOrder(orderLocal)
    }

    fun deleteAllOrders() {
        viewModelScope.launch {
            repo.deleteAllOrders()
        }
    }

    fun updateOrder(orderLocal: OrderLocal) = viewModelScope.launch {
        repo.updateOrderWithResult(orderLocal)
    }

    fun updateMachine(machineLocal: MachineLocal) = viewModelScope.launch {
        machineRepo.updateMachine(machineLocal)
    }

    private fun tickerFlow(periodMillis: Long) = flow {
        while (true) {
            emit(Unit)
            delay(periodMillis)
        }
    }

    init {
        viewModelScope.launch {
            storePreferences.userIdStore.collectLatest { _storeID.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userIdUser.collectLatest { _userID.value = it.orEmpty() }
        }
        viewModelScope.launch {
            storePreferences.userToken.collectLatest { token ->
                if (!token.isNullOrEmpty()) {
                    client.login(token)
                    _isLoggedIn.value = true
                } else {
                    _isLoggedIn.value = false
                }
            }
        }

        viewModelScope.launch {
            tickerFlow(1_000).collect {
                val machines = machineRepo.machineLocal.first()
                val orders = repo.orders.first()
                val logMachine = logMachineLocalRepository.logMachine.first()

                Log.d("TimeoutChecker", "⏰ Ticker triggered: ${machines.size} machines, ${orders.size} orders")

                checkMachineTimeouts(machines, orders, logMachine)
            }
        }
    }

    private suspend fun checkMachineTimeouts(
        machines: List<MachineLocal>,
        orders: List<OrderLocal>,
        logMachineLocal: List<LogMachineLocal>
    ) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX")
            .withZone(ZoneOffset.UTC)

        val now = Instant.now().toEpochMilli()

        for (machine in machines) {
            if (!machine.inUse || machine.timeOn == null) continue

            try {
                val timeOnMillis = Instant.from(formatter.parse(machine.timeOn)).toEpochMilli()
                val expiredMillis = timeOnMillis + machine.timer * 60_000

                if (now > expiredMillis) {
                    val relatedOrder = machine.order?.let { repo.getOrderById(it) } ?: continue
                    
                    // Do not auto-advance order step here!
                    // Mark machine with needsVerification = true instead.
                    Log.d("TimeoutChecker", "⌛ Order ${relatedOrder.id} timer expired. Waiting for BLE verification.")

                    if (!machine.needsVerification) {
                        val updatedMachine = machine.copy(
                            needsVerification = true
                        )
                        updateMachine(updatedMachine)
                        Log.d("TimeoutChecker", "📌 Tagged machine ${machine.numberMachine} for verification.")
                    }
                }
            } catch (e: Exception) {
                Log.e("TimeoutChecker", "❌ Error parsing timeOn: ${machine.timeOn}", e)
            }
        }

        // REMOVED: Redundant sync logic that caused duplication.
        // Sync is now handled exclusively by SyncWorker (WorkManager).
        if (_isLoggedIn.value) {
            repo.deleteOldSyncedOrders()
            logMachineLocalRepository.deleteOldSyncedLogMachine()
        }
    }
    private fun determineUpdatedStep(order: OrderLocal): Int {
        return when (order.typeMachineService) {
            0, 1 -> 4
            2 -> if (order.stepMachine == 2) 1 else 4
            else -> order.stepMachine
        }
    }


    fun syncNow() = viewModelScope.launch {
        SyncUtils.enqueueSync(storePreferences.context)
    }
}

fun OrderLocal.toRemoteModel(): OrderRemote {
    return OrderRemote(
        id = id,
        customerName = customerName,
        serviceName = serviceName,
        sizeMachine = sizeMachine,
        typeMachineService = typeMachineService,
        price = price,
        typePayment = typePayment,
        user = user,
        store = store,
        date = date,
        admin = admin
    )
}

fun LogMachineLocal.toRemoteModel(): LogMachineRemote {
    return LogMachineRemote(
        id = id,
        numberMachine = numberMachine,
        typeMachine = typeMachine,
        sizeMachine = sizeMachine,
        user = user,
        store = store,
        date = date
    )
}

