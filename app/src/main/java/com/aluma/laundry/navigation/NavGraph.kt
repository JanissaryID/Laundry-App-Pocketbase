package com.aluma.laundry.navigation

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
import com.aluma.laundry.bluetooth.BluetoothHelper
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.ui.view.screens.ScreenChoseStore
import com.aluma.laundry.ui.view.screens.ScreenHome
import com.aluma.laundry.ui.view.screens.ScreenLoading
import com.aluma.laundry.ui.view.screens.ScreenLogin
import com.aluma.laundry.ui.view.screens.ScreenSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    storePreferenceViewModel: StorePreferenceViewModel = koinInject(),
    bluetoothHelper: BluetoothHelper
) {
    var startDestination by remember { mutableStateOf<String?>(null) }
    var showLoading by remember { mutableStateOf(true) }

    val token by storePreferenceViewModel.token.collectAsState()
    val idUser by storePreferenceViewModel.idUser.collectAsState()
    val idStore by storePreferenceViewModel.idStore.collectAsState()
    val nameStore by storePreferenceViewModel.nameStore.collectAsState()
    val coroutineScope = rememberCoroutineScope()


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
            ScreenHome(
                onNavigate = {

                },
                onNavigateSettings = {
                    navController.navigate(Screens.Settings.route)
                }
            )
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
            val canGoBack = navController.previousBackStackEntry != null

            ScreenChoseStore(
                canGoBack = canGoBack,
                onBack = { navController.popBackStack() },
                nextScreen = {
                    if (canGoBack){
                        showLoading = true

                        // Tunda navigasi untuk tampilkan loading dulu
                        coroutineScope.launch {
                            delay(500)
                            showLoading = false

                            navController.navigate(Screens.Home.route) {
                                popUpTo(Screens.ChoseStore.route) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }
                    }else{
                        navController.navigate(Screens.Home.route) {
                            popUpTo(Screens.ChoseStore.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
        composable(Screens.Settings.route) {
            ScreenSettings(
                onBack = { navController.popBackStack() },
                onChangeStore = {
                    navController.navigate(Screens.ChoseStore.route) {
                        launchSingleTop = true
                    }
                },
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
                bluetoothHelper = bluetoothHelper
            )
        }
    }
}