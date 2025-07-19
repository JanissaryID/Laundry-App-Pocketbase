package com.aluma.laundry.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aluma.laundry.view.screens.ScreenChoseStore
import com.aluma.laundry.view.screens.ScreenHome
import com.aluma.laundry.view.screens.ScreenLogin

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Screens.Login.route) {
        composable(Screens.Home.route) {
            ScreenHome()
        }
        composable(Screens.Login.route) {
            ScreenLogin(){
                    email, password ->
                Log.d("Login", "Email: $email, Password: $password")
            }
        }
        composable(Screens.ChoseStore.route) {
            ScreenChoseStore()
        }
    }
}