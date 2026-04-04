package com.aluma.laundry.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.UUID
import kotlin.coroutines.resume

/**
 * Core BLE GATT connection manager.
 * Handles connect → subscribe notify → write command → read response → disconnect.
 *
 * All operations are connect-on-demand (not persistent).
 * Supports batched broadcasting to multiple devices.
 */
@SuppressLint("MissingPermission")
class BleConnectionManager(private val context: Context) {

    companion object {
        private const val TAG = "BleConnectionManager"

        val SERVICE_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789abc")
        val WRITE_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789abd")
        val NOTIFY_UUID: UUID = UUID.fromString("12345678-1234-1234-1234-123456789abe")
        val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        const val RESPONSE_TIMEOUT_MS = 5000L
        const val CONNECT_TIMEOUT_MS = 8000L
        const val BATCH_SIZE = 4
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    // Track active GATT connections for cleanup
    private val activeGatts = mutableListOf<BluetoothGatt>()

    // ==========================================
    // SINGLE DEVICE: connect → command → response → disconnect
    // ==========================================

    /**
     * Connect to a BLE device, send a command, wait for the Notify response, then disconnect.
     *
     * Full flow:
     * 1. getRemoteDevice(macAddress)
     * 2. connectGatt()
     * 3. discoverServices()
     * 4. Subscribe to Notify characteristic (enable CCCD)
     * 5. Write command to Write characteristic
     * 6. Wait for Notify callback (with timeout)
     * 7. Parse response → BleResult
     * 8. Disconnect & close GATT
     */
    suspend fun sendCommand(macAddress: String, command: String): BleResult =
        withContext(Dispatchers.IO) {
            val adapter = bluetoothAdapter
            if (adapter == null || !adapter.isEnabled) {
                return@withContext BleResult.Error("Bluetooth tidak aktif")
            }

            var gatt: BluetoothGatt? = null

            try {
                withTimeout(CONNECT_TIMEOUT_MS) {
                    // Deferred untuk setiap tahap GATT
                    val servicesDiscovered = CompletableDeferred<BluetoothGatt>()
                    val notifyEnabled = CompletableDeferred<Unit>()
                    val responseReceived = CompletableDeferred<String>()

                    val gattCallback = object : BluetoothGattCallback() {

                        override fun onConnectionStateChange(
                            g: BluetoothGatt,
                            status: Int,
                            newState: Int
                        ) {
                            Log.d(TAG, "[$macAddress] Connection state: $newState (status=$status)")
                            when (newState) {
                                BluetoothProfile.STATE_CONNECTED -> {
                                    Log.d(TAG, "[$macAddress] Connected, discovering services...")
                                    g.discoverServices()
                                }

                                BluetoothProfile.STATE_DISCONNECTED -> {
                                    Log.w(TAG, "[$macAddress] Disconnected (status=$status)")
                                    if (!servicesDiscovered.isCompleted) {
                                        servicesDiscovered.completeExceptionally(
                                            Exception("Disconnected during connect (status=$status)")
                                        )
                                    }
                                    if (!responseReceived.isCompleted) {
                                        responseReceived.completeExceptionally(
                                            Exception("Disconnected while waiting response")
                                        )
                                    }
                                }
                            }
                        }

                        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
                            Log.d(TAG, "[$macAddress] Services discovered (status=$status)")
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                servicesDiscovered.complete(g)
                            } else {
                                servicesDiscovered.completeExceptionally(
                                    Exception("Service discovery failed (status=$status)")
                                )
                            }
                        }

                        override fun onDescriptorWrite(
                            g: BluetoothGatt,
                            descriptor: BluetoothGattDescriptor,
                            status: Int
                        ) {
                            Log.d(TAG, "[$macAddress] Descriptor write (status=$status)")
                            if (descriptor.uuid == CCCD_UUID) {
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    notifyEnabled.complete(Unit)
                                } else {
                                    notifyEnabled.completeExceptionally(
                                        Exception("CCCD write failed (status=$status)")
                                    )
                                }
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onCharacteristicChanged(
                            g: BluetoothGatt,
                            characteristic: BluetoothGattCharacteristic
                        ) {
                            if (characteristic.uuid == NOTIFY_UUID) {
                                val value = characteristic.value?.let { String(it) } ?: ""
                                Log.d(TAG, "[$macAddress] Notify received: $value")
                                if (!responseReceived.isCompleted) {
                                    responseReceived.complete(value)
                                }
                            }
                        }

                        override fun onCharacteristicChanged(
                            g: BluetoothGatt,
                            characteristic: BluetoothGattCharacteristic,
                            value: ByteArray
                        ) {
                            if (characteristic.uuid == NOTIFY_UUID) {
                                val response = String(value)
                                Log.d(TAG, "[$macAddress] Notify received (new API): $response")
                                if (!responseReceived.isCompleted) {
                                    responseReceived.complete(response)
                                }
                            }
                        }
                    }

                    // STEP 1: Connect GATT
                    val device = adapter.getRemoteDevice(macAddress)
                    Log.d(TAG, "[$macAddress] Connecting GATT...")
                    gatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
                    synchronized(activeGatts) { activeGatts.add(gatt!!) }

                    // STEP 2: Wait for services discovered
                    val connectedGatt = servicesDiscovered.await()

                    // STEP 3: Subscribe to Notify
                    val service = connectedGatt.getService(SERVICE_UUID)
                        ?: throw Exception("Service UUID not found")

                    val notifyChar = service.getCharacteristic(NOTIFY_UUID)
                        ?: throw Exception("Notify characteristic not found")

                    connectedGatt.setCharacteristicNotification(notifyChar, true)

                    val descriptor = notifyChar.getDescriptor(CCCD_UUID)
                        ?: throw Exception("CCCD descriptor not found")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        connectedGatt.writeDescriptor(
                            descriptor,
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        @Suppress("DEPRECATION")
                        connectedGatt.writeDescriptor(descriptor)
                    }

                    notifyEnabled.await()
                    Log.d(TAG, "[$macAddress] Notify enabled, writing command: $command")

                    // STEP 4: Write command
                    val writeChar = service.getCharacteristic(WRITE_UUID)
                        ?: throw Exception("Write characteristic not found")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        connectedGatt.writeCharacteristic(
                            writeChar,
                            command.toByteArray(Charsets.UTF_8),
                            BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        writeChar.value = command.toByteArray(Charsets.UTF_8)
                        @Suppress("DEPRECATION")
                        connectedGatt.writeCharacteristic(writeChar)
                    }

                    // STEP 5: Wait for response with timeout
                    val response = withTimeout(RESPONSE_TIMEOUT_MS) {
                        responseReceived.await()
                    }

                    // STEP 6: Parse and return
                    Log.d(TAG, "[$macAddress] Response: $response → parsing...")
                    BleResult.parse(response)
                }
            } catch (e: Exception) {
                Log.e(TAG, "[$macAddress] Error: ${e.message}", e)
                BleResult.Error(e.message ?: "Unknown BLE error")
            } finally {
                // STEP 7: Always disconnect and close
                try {
                    gatt?.disconnect()
                    gatt?.close()
                } catch (e: Exception) {
                    Log.w(TAG, "[$macAddress] Error closing GATT: ${e.message}")
                }
                synchronized(activeGatts) { gatt?.let { activeGatts.remove(it) } }
                Log.d(TAG, "[$macAddress] GATT closed")
            }
        }

    // ==========================================
    // BATCHED BROADCAST
    // ==========================================

    /**
     * Send a command to multiple devices in batches.
     * Devices within a batch are connected in parallel.
     * Batches are processed sequentially to respect Android BLE limits.
     *
     * @param macAddresses List of MAC addresses to send to
     * @param command The command to send (e.g. "stat_now")
     * @param batchSize Number of devices per batch (default 4)
     * @return Map of MAC address to BleResult
     */
    suspend fun broadcastCommand(
        macAddresses: List<String>,
        command: String,
        batchSize: Int = BATCH_SIZE
    ): Map<String, BleResult> = coroutineScope {
        val results = mutableMapOf<String, BleResult>()

        macAddresses.chunked(batchSize).forEach { batch ->
            Log.d(TAG, "Broadcasting batch: ${batch.size} devices")

            val batchResults = batch.map { mac ->
                async(Dispatchers.IO) {
                    mac to sendCommand(mac, command)
                }
            }.awaitAll()

            batchResults.forEach { (mac, result) ->
                results[mac] = result
            }

            Log.d(TAG, "Batch complete: ${batchResults.size} results")
        }

        results
    }

    // ==========================================
    // CLEANUP
    // ==========================================

    /**
     * Disconnect and close all active GATT connections.
     * Called on app pause/destroy.
     */
    fun disconnectAll() {
        synchronized(activeGatts) {
            Log.d(TAG, "Disconnecting all: ${activeGatts.size} connections")
            activeGatts.forEach { gatt ->
                try {
                    gatt.disconnect()
                    gatt.close()
                } catch (e: Exception) {
                    Log.w(TAG, "Error closing GATT: ${e.message}")
                }
            }
            activeGatts.clear()
        }
    }
}
