package com.aluma.laundry.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.view.screens.ScreenChoseStore
import com.aluma.laundry.view.screens.ScreenHome
import com.aluma.laundry.view.screens.ScreenLoading
import com.aluma.laundry.view.screens.ScreenLogin
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    storePreferenceViewModel: StorePreferenceViewModel = koinInject()
) {
    var startDestination by remember { mutableStateOf<String?>(null) }
    var showLoading by remember { mutableStateOf(true) }

    val token by storePreferenceViewModel.token.collectAsState()
    val idUser by storePreferenceViewModel.idUser.collectAsState()
    val idStore by storePreferenceViewModel.idStore.collectAsState()

    // Delay loading screen untuk UX (500ms)
    LaunchedEffect(Unit) {
        delay(500)
        showLoading = false
    }

    // Hitung start destination setelah semua data tersedia
    LaunchedEffect(token, idUser, idStore) {
        startDestination = when {
            !token.isNullOrEmpty() && !idUser.isNullOrEmpty() && !idStore.isNullOrEmpty() -> Screens.Home.route
            !token.isNullOrEmpty() && !idUser.isNullOrEmpty() && idStore.isNullOrEmpty() -> Screens.ChoseStore.route
            else -> Screens.Login.route
        }
    }

    // Tampilkan screen loading jika loading belum selesai atau start destination belum siap
    if (startDestination == null || showLoading) {
        ScreenLoading()
        return
    }

    // Setelah loading selesai dan tujuan siap
    NavHost(navController = navController, startDestination = startDestination!!) {
        composable(Screens.Home.route) {
            ScreenHome()
        }
        composable(Screens.Login.route) {
            ScreenLogin(
                onSuccess = {
                    navController.navigate(Screens.ChoseStore.route) {
                        popUpTo(Screens.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screens.ChoseStore.route) {
            ScreenChoseStore(
                nextScreen = {
                    navController.navigate(Screens.Home.route) {
                        popUpTo(Screens.Home.route) {
                            inclusive = true // Hapus Login dari backstack
                        }
                        launchSingleTop = true // Hindari multiple instance jika sudah di stack
                    }
                }
            )
        }
    }
}