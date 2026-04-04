package com.aluma.laundry

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.aluma.laundry.bluetooth.BluetoothHelper
import com.aluma.laundry.navigation.AppNavHost
import com.aluma.laundry.ui.theme.MyApplicationTheme

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MainActivity : ComponentActivity(), KoinComponent {

    private lateinit var bluetoothHelper: BluetoothHelper
    private val bleConnectionManager: com.aluma.laundry.bluetooth.BleConnectionManager by inject()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            )
        )

        bluetoothHelper = BluetoothHelper(this)

        setContent {
            MyApplicationTheme(
                darkTheme = false
            ) {
                AppNavHost(bluetoothHelper = bluetoothHelper)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        bleConnectionManager.disconnectAll()
    }
}