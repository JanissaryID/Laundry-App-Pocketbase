package com.aluma.laundry.data.order.local

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.machine.local.MachineLocalRepository
import com.aluma.laundry.data.machine.model.MachineLocal
import com.aluma.laundry.data.order.model.OrderLocal
import com.aluma.laundry.data.order.model.OrderRemote
import com.aluma.laundry.data.order.remote.OrderRemoteRepository
import com.aluma.laundry.data.order.utils.SyncStatus
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

class OrderLocalViewModel(
    private val repo: OrderLocalRepository,
    private val machineRepo: MachineLocalRepository,
    private val orderRemoteRepository: OrderRemoteRepository,
    private val client: PocketbaseClient,
    private val storePreferences: StorePreferences
) : ViewModel() {

    private val _maxStep = MutableStateFlow(4)
    private val _isLoggedIn = MutableStateFlow(false)

    private val sharingStarted = SharingStarted.WhileSubscribed(5_000)

    val ordersFilter: StateFlow<List<OrderLocal>> = _maxStep
        .combine(repo.orders) { maxStep, allOrders ->
            allOrders.filter { it.stepMachine < maxStep }
                .reversed()
        }
        .stateIn(viewModelScope, sharingStarted, emptyList())

    val orders: StateFlow<List<OrderLocal>> = repo.orders
        .stateIn(viewModelScope, sharingStarted, emptyList())

    private val _selectedOrder = MutableStateFlow<OrderLocal?>(null)
    val selectedOrder: StateFlow<OrderLocal?> = _selectedOrder

    fun setSelectedOrder(order: OrderLocal?) {
        _selectedOrder.value = order
    }

    fun addOrder(orderLocal: OrderLocal) = viewModelScope.launch {
        repo.addOrder(orderLocal)
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

                Log.d("TimeoutChecker", "⏰ Ticker triggered: ${machines.size} machines, ${orders.size} orders")

                checkMachineTimeouts(machines, orders)
            }
        }
    }

    private suspend fun checkMachineTimeouts(
        machines: List<MachineLocal>,
        orders: List<OrderLocal>
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
                    val updatedStep = determineUpdatedStep(relatedOrder)

                    Log.d("TimeoutChecker", "⌛ Order ${relatedOrder.id} expired. Step: ${relatedOrder.stepMachine} → $updatedStep")

                    val updatedOrder = relatedOrder.copy(
                        stepMachine = updatedStep,
                        numberMachine = 0
                    )

                    Log.d("TimeoutChecker", "📌 About to update order ${updatedOrder.id} to step=${updatedOrder.stepMachine}")
                    val updatedRows = repo.updateOrderWithResult(updatedOrder)
                    val reloaded = repo.getOrderById(relatedOrder.id)
                    Log.d("Checker", "🔁 Reloaded stepMachine: ${reloaded?.stepMachine}")
                    Log.d("TimeoutChecker", "📦 Updated rows: $updatedRows")
                    Log.d("TimeoutChecker", "📦 Updated rows 2: $updatedOrder")
                    if (updatedRows > 0) {
                        val updatedMachine = machine.copy(
                            inUse = false,
                            order = null,
                            timeOn = null
                        )
                        updateMachine(updatedMachine)
                        Log.d("TimeoutChecker", "✅ Reset machine ${machine.numberMachine} done.")
                    } else {
                        Log.w("TimeoutChecker", "⚠️ Order ${relatedOrder.id} failed to update. Machine not updated.")
                    }
                }
            } catch (e: Exception) {
                Log.e("TimeoutChecker", "❌ Error parsing timeOn: ${machine.timeOn}", e)
            }
        }

        if (_isLoggedIn.value) {
            syncPendingOrders(orders)
        }
    }

    private fun determineUpdatedStep(order: OrderLocal): Int {
        return when (order.typeMachineService) {
            0, 1 -> 4
            2 -> if (order.stepMachine == 2) 1 else 4
            else -> order.stepMachine
        }
    }

    private suspend fun syncPendingOrders(orders: List<OrderLocal>) {
        val pendingOrders = orders.filter {
            it.syncStatus == SyncStatus.PENDING || it.syncStatus == SyncStatus.FAILED
        }

        for (order in pendingOrders) {
            try {
                orderRemoteRepository.createOrder(order.toRemoteModel())
                repo.updateSyncStatusOnly(order.id,SyncStatus.SYNCED)
                Log.d("SyncChecker", "✅ Synced order ${order.id}")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                repo.updateSyncStatusOnly(order.id,SyncStatus.FAILED)
                Log.e("SyncChecker", "❌ Failed to sync order ${order.id}", e)
            }
        }
    }

    fun syncNow() = viewModelScope.launch {
        val latest = repo.orders.first()
        syncPendingOrders(latest)
    }
}

fun OrderLocal.toRemoteModel(): OrderRemote {
    return OrderRemote(
        customerName = customerName,
        serviceName = serviceName,
        sizeMachine = sizeMachine,
        typeMachineService = typeMachineService,
        price = price,
        typePayment = typePayment,
        user = user,
        store = store
    )
}
