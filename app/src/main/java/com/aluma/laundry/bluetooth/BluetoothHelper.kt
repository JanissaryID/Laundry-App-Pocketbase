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
            val allGranted = permissions.all { it.value == true }
            if (allGranted) {
                checkAndEnableBluetooth()
            }
        }

    private val enableBluetoothLauncher =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                onBluetoothReady?.invoke()
            }
        }

    fun requestBluetooth(onReady: () -> Unit) {
        this.onBluetoothReady = onReady

        if (!hasBluetoothPermissions()) {
            permissionLauncher.launch(requiredPermissions())
        } else {
            checkAndEnableBluetooth()
        }
    }

    private fun checkAndEnableBluetooth() {
        if (bluetoothAdapter?.isEnabled == true) {
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
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.BLUETOOTH)
        }
    }

    fun getPairedDevices(): Set<BluetoothDevice> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                emptySet()
            } else {
                bluetoothAdapter?.bondedDevices ?: emptySet()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            emptySet()
        }
    }

    fun getPairedPrinterDevices(): Set<BluetoothDevice> {
        val printerKeywords = listOf("printer", "bt", "pos", "zebra", "xprinter", "bixolon", "inner", "rp", "rpp", "RPP02N")

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                emptySet()
            } else {
                bluetoothAdapter?.bondedDevices
                    ?.filter { device ->
                        val name = device.name?.lowercase() ?: ""
                        printerKeywords.any { keyword -> name.contains(keyword) }
                    }
                    ?.toSet() ?: emptySet()
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            emptySet()
        }
    }
}

