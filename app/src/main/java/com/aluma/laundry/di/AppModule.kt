package com.aluma.laundry.di

import androidx.activity.ComponentActivity
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.store.StoreViewModel
import com.aluma.laundry.viewmodel.MainViewModel
import com.aluma.laundry.user.UserViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { StorePreferences(androidContext()) }
//    factory { (activity: ComponentActivity) -> BluetoothHelper(activity) }
    single { UserViewModel(storePreferences = get()) }
    single { StorePreferenceViewModel(storePreferences = get()) }
    single { StoreViewModel(storePreferences = get()) }
}