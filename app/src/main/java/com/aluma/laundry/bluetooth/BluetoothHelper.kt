package com.aluma.laundry.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat


class BluetoothHelper(private val activity: ComponentActivity) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var onBluetoothReady: (() -> Unit)? = null

    private val permissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = requiredPermissions().all {
                permissions[it] == true
            }
            if (allGranted) {
                Log.d("BluetoothHelper", "Semua permission sudah diberikan")
                checkAndEnableBluetooth()
            } else {
                Log.w("BluetoothHelper", "Permission Bluetooth belum lengkap")
            }
        }

    private val enableBluetoothLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.d("BluetoothHelper", "Bluetooth berhasil diaktifkan")
                onBluetoothReady?.invoke()
            } else {
                Log.w("BluetoothHelper", "Bluetooth tidak diaktifkan oleh pengguna")
            }
        }

    fun requestBluetooth(onReady: () -> Unit) {
        this.onBluetoothReady = onReady

        if (!hasBluetoothPermissions()) {
            Log.d("BluetoothHelper", "Minta permission Bluetooth")
            permissionLauncher.launch(requiredPermissions())
        } else {
            Log.d("BluetoothHelper", "Permission Bluetooth sudah OK")
            checkAndEnableBluetooth()
        }
    }

    private fun checkAndEnableBluetooth() {
        if (bluetoothAdapter?.isEnabled == true) {
            Log.d("BluetoothHelper", "Bluetooth sudah aktif")
            onBluetoothReady?.invoke()
        } else {
            val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableIntent)
        }
    }

    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    fun hasBluetoothPermissions(): Boolean {
        return requiredPermissions().all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    fun getPairedDevices(): Set<BluetoothDevice> {
        return if (!hasBluetoothPermissions()) {
            Log.w("BluetoothHelper", "getPairedDevices: Tidak punya permission")
            emptySet()
        } else {
            try {
                bluetoothAdapter?.bondedDevices ?: emptySet()
            } catch (e: SecurityException) {
                Log.e("BluetoothHelper", "getPairedDevices: SecurityException", e)
                emptySet()
            }
        }
    }

    fun getPairedPrinterDevices(): Set<BluetoothDevice> {
        val printerKeywords = listOf("printer", "bt", "pos", "zebra", "xprinter", "bixolon", "inner", "rp", "rpp", "rpp02n")

        return if (!hasBluetoothPermissions()) {
            Log.w("BluetoothHelper", "getPairedPrinterDevices: Tidak punya permission")
            emptySet()
        } else {
            try {
                bluetoothAdapter?.bondedDevices
                    ?.filter { device ->
                        val name = device.name?.lowercase() ?: ""
                        printerKeywords.any { keyword -> name.contains(keyword) }
                    }
                    ?.toSet() ?: emptySet()
            } catch (e: SecurityException) {
                Log.e("BluetoothHelper", "getPairedPrinterDevices: SecurityException", e)
                emptySet()
            }
        }
    }
}


