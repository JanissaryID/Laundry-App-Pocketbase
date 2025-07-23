package com.aluma.laundry.data.room.order

import android.util.Log
import com.aluma.laundry.data.room.machine.MachineRepository
import com.aluma.laundry.data.room.machine.MachineRoom
import com.aluma.laundry.data.room.machine.MachineRoomViewModel
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import io.github.agrevster.pocketbaseKotlin.dsl.login
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class OrderRoomViewModel(
    private val repo: OrderRepository,
    private val machineRepo: MachineRepository
) : ViewModel() {

    private val _maxStep = MutableStateFlow(4)

    val ordersFilter: StateFlow<List<OrderRoom>> = _maxStep
        .combine(repo.orders) { maxStep, allOrders ->
            allOrders.filter { it.stepMachine < maxStep }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val orders: StateFlow<List<OrderRoom>> = repo.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedOrder = MutableStateFlow<OrderRoom?>(null)
    val selectedOrder: StateFlow<OrderRoom?> = _selectedOrder

    fun setSelectedOrder(order: OrderRoom?) {
        _selectedOrder.value = order
    }

    fun addOrder(orderRoom: OrderRoom) = viewModelScope.launch {
        repo.addOrder(orderRoom)
    }

    fun deleteOrder(orderRoom: OrderRoom) = viewModelScope.launch {
        repo.deleteOrder(orderRoom)
    }

    fun updateOrder(orderRoom: OrderRoom) = viewModelScope.launch {
        repo.updateOrder(orderRoom)
    }

    fun updateMachine(machineRoom: MachineRoom) = viewModelScope.launch {
        machineRepo.updateMachine(machineRoom)
    }

    private fun tickerFlow(periodMillis: Long) = flow {
        while (true) {
            emit(Unit) // trigger setiap periode
            delay(periodMillis)
        }
    }

    init {
        viewModelScope.launch {
            combine(
                tickerFlow(10_000),             // ⏱ timer pemicu
                machineRepo.machineRoom,        // 🧺 data mesin
                repo.orders                     // 📦 data order
            ) { _, machines, orders ->
                machines to orders              // hasil combine: Pair
            }
                .collect { (machines, orders) ->    // 🎯 eksekusi setiap 10 detik
                    Log.d("TimeoutChecker", "Triggered. Machines: ${machines.size}, Orders: ${orders.size}")
                    checkMachineTimeouts(machines, orders)
                }
        }
    }

    private fun checkMachineTimeouts(
        machines: List<MachineRoom>,
        orders: List<OrderRoom>
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
}
