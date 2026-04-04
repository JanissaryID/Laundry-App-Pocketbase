package com.aluma.laundry.bluetooth

import android.util.Log
import com.aluma.laundry.data.machine.model.MachineLocal
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel that manages BLE state for machine communication.
 *
 * Responsibilities:
 * - Broadcast status check to all machines (batched)
 * - Fresh status check for order-filtered machines
 * - Turn on a machine via BLE
 * - Check single machine status (for verification)
 */
class BleViewModel(
    private val bleConnectionManager: BleConnectionManager
) : ViewModel() {

    companion object {
        private const val TAG = "BleViewModel"
    }

    // ==========================================
    // STATE: Status per machine
    // ==========================================

    data class MachineStatus(
        val machineId: String,
        val macAddress: String,
        val stage: Int = -1,          // -1 = not yet checked
        val transactionId8: String = "",
        val isChecking: Boolean = false,
        val error: String? = null
    )

    private val _machineStatuses = MutableStateFlow<Map<String, MachineStatus>>(emptyMap())
    val machineStatuses: StateFlow<Map<String, MachineStatus>> = _machineStatuses

    // Loading state for app startup broadcast (Phase A)
    private val _isBroadcasting = MutableStateFlow(false)
    val isBroadcasting: StateFlow<Boolean> = _isBroadcasting

    // Loading state for order-time check (Phase B)
    private val _isCheckingForOrder = MutableStateFlow(false)
    val isCheckingForOrder: StateFlow<Boolean> = _isCheckingForOrder

    // ==========================================
    // PHASE A: Background broadcast at app start
    // ==========================================

    /**
     * Send stat_now to ALL machines. Used at app startup to cache machine statuses.
     * Machines are batched (4 per batch).
     */
    fun broadcastStatusCheck(machines: List<MachineLocal>) {
        viewModelScope.launch {
            _isBroadcasting.value = true
            Log.d(TAG, "Starting broadcast status check for ${machines.size} machines")

            val macMap = machines
                .filter { !it.bluetoothAddress.isNullOrEmpty() }
                .associate { it.bluetoothAddress!! to it.id }

            if (macMap.isEmpty()) {
                Log.w(TAG, "No machines with MAC addresses to check")
                _isBroadcasting.value = false
                return@launch
            }

            val results = withContext(Dispatchers.IO) {
                bleConnectionManager.broadcastCommand(
                    macAddresses = macMap.keys.toList(),
                    command = "stat_now",
                    batchSize = BleConnectionManager.BATCH_SIZE
                )
            }

            // Update statuses
            val statuses = mutableMapOf<String, MachineStatus>()
            results.forEach { (mac, result) ->
                val machineId = macMap[mac] ?: return@forEach
                statuses[machineId] = resultToStatus(machineId, mac, result)
            }
            _machineStatuses.value = statuses

            _isBroadcasting.value = false
            Log.d(TAG, "Broadcast complete: ${statuses.size} statuses")
        }
    }

    // ==========================================
    // PHASE B: Fresh check for order-time
    // Only 2-4 machines (filtered by type/size)
    // ==========================================

    /**
     * Fresh status check for a small set of machines (2-4).
     * Used when user opens OrderBottomSheetInformation.
     */
    fun checkStatusForOrder(machines: List<MachineLocal>) {
        viewModelScope.launch {
            _isCheckingForOrder.value = true
            Log.d(TAG, "Checking status for order: ${machines.size} machines")

            val macMap = machines
                .filter { !it.bluetoothAddress.isNullOrEmpty() }
                .associate { it.bluetoothAddress!! to it.id }

            if (macMap.isEmpty()) {
                _isCheckingForOrder.value = false
                return@launch
            }

            // Small set → all in one batch
            val results = withContext(Dispatchers.IO) {
                bleConnectionManager.broadcastCommand(
                    macAddresses = macMap.keys.toList(),
                    command = "stat_now",
                    batchSize = macMap.size
                )
            }

            // Merge with existing statuses
            val updated = _machineStatuses.value.toMutableMap()
            results.forEach { (mac, result) ->
                val machineId = macMap[mac] ?: return@forEach
                updated[machineId] = resultToStatus(machineId, mac, result)
            }
            _machineStatuses.value = updated

            _isCheckingForOrder.value = false
            Log.d(TAG, "Order check complete")
        }
    }

    // ==========================================
    // TURN ON MACHINE
    // ==========================================

    /**
     * Send ON command to a machine.
     * Command format: "<machineNumber>|<durationMinutes>|<transactionId>"
     */
    suspend fun turnOnMachine(
        machine: MachineLocal,
        durationMinutes: Int,
        transactionId: String
    ): BleResult {
        val mac = machine.bluetoothAddress
            ?: return BleResult.Error("No MAC address")
        val command = "${machine.numberMachine}|${durationMinutes}|${transactionId}"
        Log.d(TAG, "Turning on machine ${machine.numberMachine}: $command")
        return withContext(Dispatchers.IO) {
            bleConnectionManager.sendCommand(mac, command)
        }
    }

    // ==========================================
    // CHECK SINGLE MACHINE STATUS (for verification)
    // ==========================================

    /**
     * Check status of a single machine.
     * Used when countdown finishes to verify completion.
     */
    suspend fun checkMachineStatus(machine: MachineLocal): BleResult {
        val mac = machine.bluetoothAddress
            ?: return BleResult.Error("No MAC address")
        Log.d(TAG, "Checking status of machine ${machine.numberMachine}")
        return withContext(Dispatchers.IO) {
            bleConnectionManager.sendCommand(mac, "stat_now")
        }
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private fun resultToStatus(machineId: String, mac: String, result: BleResult): MachineStatus {
        return when (result) {
            is BleResult.Status -> MachineStatus(
                machineId = machineId,
                macAddress = mac,
                stage = result.stage,
                transactionId8 = result.transactionId8
            )

            is BleResult.Error -> MachineStatus(
                machineId = machineId,
                macAddress = mac,
                error = result.message
            )

            else -> MachineStatus(
                machineId = machineId,
                macAddress = mac,
                error = "Unexpected response"
            )
        }
    }

    fun resetStatuses() {
        _machineStatuses.value = emptyMap()
    }

    override fun onCleared() {
        super.onCleared()
        bleConnectionManager.disconnectAll()
    }
}
