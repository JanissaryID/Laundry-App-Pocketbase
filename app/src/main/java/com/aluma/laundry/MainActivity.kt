package com.aluma.laundry

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.aluma.laundry.bluetooth.BluetoothHelper
import com.aluma.laundry.navigation.AppNavHost
import com.aluma.laundry.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var bluetoothHelper: BluetoothHelper

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        bluetoothHelper = BluetoothHelper(this)

        setContent {
            MyApplicationTheme(
                darkTheme = false
            ) {
                AppNavHost(bluetoothHelper = bluetoothHelper)
            }
        }
    }
}