package com.aluma.owner.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aluma.owner.data.datastore.StorePreferenceViewModel
import com.aluma.owner.ui.view.screens.ScreenHomeOwner
import com.aluma.owner.ui.view.screens.ScreenListOrders
import com.aluma.owner.ui.view.screens.ScreenLoading
import com.aluma.owner.ui.view.screens.ScreenLogin
import com.aluma.owner.ui.view.screens.ScreenMachine
import com.aluma.owner.ui.view.screens.ScreenServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.aluma.owner.utils.safePopBackStack
import org.koin.compose.koinInject

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    storePreferenceViewModel: StorePreferenceViewModel = koinInject(),
) {
    var startDestination by remember { mutableStateOf<String?>(null) }
    var showLoading by remember { mutableStateOf(true) }

    val token by storePreferenceViewModel.token.collectAsState()
    val idUser by storePreferenceViewModel.idUser.collectAsState()
    val idStore by storePreferenceViewModel.idStore.collectAsState()
    val coroutineScope = rememberCoroutineScope()


    // Delay loading screen untuk UX (500ms)
    LaunchedEffect(Unit) {
        delay(500)
        showLoading = false
    }

    // Hitung start destination setelah semua data tersedia
    LaunchedEffect(token, idUser, idStore) {
        startDestination = when {
            !token.isNullOrEmpty() && !idUser.isNullOrEmpty() -> Screens.HomeOwner.route
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
        composable(Screens.HomeOwner.route) {
            ScreenHomeOwner(
                onLogout = {
                    showLoading = true
                    storePreferenceViewModel.clearData()

                    // Tunda navigasi untuk tampilkan loading dulu
                    coroutineScope.launch {
                        delay(500)
                        showLoading = false

                        navController.navigate(Screens.Login.route) {
                            popUpTo(0)
                            launchSingleTop = true // Hindari multiple instance jika sudah di stack
                        }
                    }
                },
                onListOrder = {
                    navController.navigate(Screens.Orders.route)
                },
                onListService = {
                    navController.navigate(Screens.Services.route)
                },
                onListMachine = {
                    navController.navigate(Screens.Machines.route)
                }
            )
        }
        composable(Screens.Machines.route) {
            ScreenMachine(
                onBack = { navController.safePopBackStack() },
            )
        }
        composable(Screens.Orders.route) {
            ScreenListOrders(
                onBack = { navController.safePopBackStack() },
            )
        }
        composable(Screens.Services.route) {
            ScreenServices(
                onBack = { navController.safePopBackStack() },
            )
        }
        composable(Screens.Login.route) {
            ScreenLogin(
                onSuccess = {
                    showLoading = true
                    coroutineScope.launch {
                        delay(500)
                        showLoading = false
                        navController.navigate(Screens.HomeOwner.route) {
                            popUpTo(0)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}