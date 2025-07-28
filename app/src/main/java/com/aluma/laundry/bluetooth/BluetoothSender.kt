package com.aluma.laundry.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import java.io.OutputStream
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
        Log.d(TAG, "Mulai kirim data ke ESP - Address: $address | Message: $message")
        return try {
            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
            if (!bluetoothAdapter.isEnabled) {
                Log.e(TAG, "Bluetooth belum aktif")
                return false
            }

            val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(address)
            Log.d(TAG, "Device ditemukan: ${device.name} (${device.address})")

            // UUID SPP standar (Serial Port Profile)
            val uuid = device.uuids?.firstOrNull()?.uuid
                ?: UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            Log.d(TAG, "UUID yang digunakan: $uuid")

            val socket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothAdapter.cancelDiscovery() // Matikan discovery untuk koneksi yang lebih stabil
            Log.d(TAG, "Menghubungkan ke socket...")
            socket.connect()
            Log.d(TAG, "Terhubung ke ESP")

            val outputStream: OutputStream = socket.outputStream
            val finalMessage = "$message\n"
            outputStream.write(finalMessage.toByteArray(Charsets.UTF_8))
            outputStream.flush()
            Log.d(TAG, "Data berhasil dikirim: $finalMessage")

            socket.close()
            Log.d(TAG, "Socket ditutup")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengirim data ke ESP: ${e.message}", e)
            false
        }
    }
}
