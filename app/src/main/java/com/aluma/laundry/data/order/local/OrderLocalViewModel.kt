package com.aluma.laundry.data.order.local

import android.util.Log
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.machine.local.MachineRepository
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

class OrderLocalViewModel(
    private val repo: OrderLocalRepository,
    private val machineRepo: MachineRepository,
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

    fun updateOrder(orderLocal: OrderLocal) = viewModelScope.launch {
        repo.updateOrder(orderLocal)
    }

    fun updateMachine(machineLocal: MachineLocal) = viewModelScope.launch {
        machineRepo.updateMachine(machineLocal)
    }

    private fun tickerFlow(periodMillis: Long) = flow {
        while (true) {
            emit(Unit) // trigger setiap periode
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
            combine(
                tickerFlow(10_000),
                machineRepo.machineLocal,
                repo.orders
            ) { _, machines, orders ->
                machines to orders
            }
                .collect { (machines, orders) ->
                    Log.d("TimeoutChecker", "Triggered. Machines: ${machines.size}, Orders: ${orders.size}")
                    checkMachineTimeouts(machines, orders)
                    if (_isLoggedIn.value) {
                        syncPendingOrders(orders)
                    }
                }
        }
    }

    private fun checkMachineTimeouts(
        machines: List<MachineLocal>,
        orders: List<OrderLocal>
    ) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX")
            .withZone(ZoneOffset.UTC)

        val now = Instant.now().toEpochMilli()

        machines.filter { it.inUse && it.timeOn != null }.forEach { machine ->
            try {
                val timeOnMillis = Instant.from(formatter.parse(machine.timeOn!!)).toEpochMilli()
                val expiredMillis = timeOnMillis + machine.timer * 60_000

                if (now > expiredMillis) {
                    val relatedOrder = orders.find { it.id == machine.order }

                    Log.d("TimeoutChecker", "Machine Order ID: ${machine.order}")
                    Log.d("TimeoutChecker", "Related Order Found: ${relatedOrder != null}")
                    relatedOrder?.let {
                        Log.d("TimeoutChecker", "Before Update - ID: ${it.id}, stepMachine: ${it.stepMachine}, numberMachine: ${it.numberMachine}, typeMachineService: ${it.typeMachineService}")
                    }

                    if (relatedOrder != null) {
                        val updatedStep = when (relatedOrder.typeMachineService) {
                            0, 1 -> 4
                            2 -> if (relatedOrder.stepMachine == 2) 1 else 4
                            else -> relatedOrder.stepMachine
                        }

                        val updatedOrder = relatedOrder.copy(
                            stepMachine = updatedStep,
                            numberMachine = 0
                        )

                        Log.d("TimeoutChecker", "After Update - ID: ${updatedOrder.id}, stepMachine: ${updatedOrder.stepMachine}, numberMachine: ${updatedOrder.numberMachine}")
                        updateOrder(updatedOrder)

                        val updatedMachine = machine.copy(
                            inUse = false,
                            order = null,
                            timeOn = null
                        )
                        updateMachine(updatedMachine)

                        Log.d("TimeoutChecker", "Machine ${machine.numberMachine} expired. Reset done.")
                    }
                }
            } catch (e: Exception) {
                Log.e("TimeoutChecker", "Error parsing timeOn: ${machine.timeOn}", e)
            }
        }
    }

    private suspend fun syncPendingOrders(orders: List<OrderLocal>) {
        val pendingOrders = orders.filter {
            it.syncStatus == SyncStatus.PENDING || it.syncStatus == SyncStatus.FAILED
        }

        for (order in pendingOrders) {
            try {
                orderRemoteRepository.createOrder(order.toRemoteModel())
                repo.updateOrder(order.copy(syncStatus = SyncStatus.SYNCED))
                Log.d("SyncChecker", "✅ Synced order ${order.id}")
            } catch (e: CancellationException) {
                throw e // biarkan propagasi
            } catch (e: Exception) {
                repo.updateOrder(order.copy(syncStatus = SyncStatus.FAILED))
                Log.e("SyncChecker", "❌ Failed to sync order ${order.id}", e)
            }
        }
    }

//    fun syncNow() = viewModelScope.launch {
//        syncPendingOrders(repo.orders)
//    }
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