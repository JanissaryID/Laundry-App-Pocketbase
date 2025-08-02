package com.aluma.owner

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.aluma.owner.navigation.AppNavHost
import com.aluma.owner.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            MyApplicationTheme(
                darkTheme = false
            ) {
                AppNavHost()
            }
        }
    }
}