package com.aluma.laundry.di

import androidx.activity.ComponentActivity
import com.aluma.laundry.viewmodel.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
//    single { UserPreferences(androidContext()) }
//    factory { (activity: ComponentActivity) -> BluetoothHelper(activity) }
    single { MainViewModel() }
}