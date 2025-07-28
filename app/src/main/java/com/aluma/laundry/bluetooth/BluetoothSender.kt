package com.aluma.laundry.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import java.util.UUID

class BluetoothSender {

    private val TAG = "BluetoothSender"

    @SuppressLint("MissingPermission")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendToESP(
        context: Context,
        address: String,
        message: String
    ): Boolean {
        var result = false

        Log.d(TAG, "Mulai kirim data ke ESP - Address: $address | Message: $message")

        try {
            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter

            if (!bluetoothAdapter.isEnabled) {
                Log.e(TAG, "Bluetooth belum aktif")
                return false
            }

            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            Log.d(TAG, "Device ditemukan: ${device.name} (${device.address})")

            val uuid = device.uuids?.firstOrNull()?.uuid
                ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            Log.d(TAG, "UUID yang digunakan: $uuid")

            val socket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothAdapter.cancelDiscovery()
            Log.d(TAG, "Menghubungkan ke socket...")
            socket.connect()
            Log.d(TAG, "Terhubung ke ESP")

            val outputStream = socket.outputStream
            val inputStream = socket.inputStream

            val finalMessage = "$message\n"
            outputStream.write(finalMessage.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            Log.d(TAG, "Data berhasil dikirim: $finalMessage")

            // Baca response dari ESP32
            val buffer = ByteArray(1024)
            val response = StringBuilder()
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < 2000) {
                if (inputStream.available() > 0) {
                    val bytesRead = inputStream.read(buffer)
                    val part = String(buffer, 0, bytesRead)
                    response.append(part)
                    if (part.contains("\n")) break
                }
            }

            val callback = response.toString().trim()
            Log.d(TAG, "Callback dari ESP: $callback")

            result = callback.contains("stat:true", ignoreCase = true)

            socket.close()
            Log.d(TAG, "Socket ditutup")

        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengirim data ke ESP: ${e.message}", e)
        }

        return result
    }
}


