package com.aluma.laundry.di

import androidx.activity.ComponentActivity
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.viewmodel.MainViewModel
import com.aluma.laundry.viewmodel.UserViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { StorePreferences(androidContext()) }
//    factory { (activity: ComponentActivity) -> BluetoothHelper(activity) }
    single { UserViewModel(storePreferences = get()) }
}