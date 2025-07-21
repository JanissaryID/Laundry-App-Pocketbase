package com.aluma.laundry.di

import com.aluma.laundry.data.api.machine.MachineViewModel
import com.aluma.laundry.data.api.service.ServiceViewModel
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.api.store.StoreViewModel
import com.aluma.laundry.data.api.user.UserViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { StorePreferences(androidContext()) }
    single { UserViewModel(storePreferences = get()) }
    single { StorePreferenceViewModel(storePreferences = get()) }
    single { StoreViewModel(storePreferences = get()) }
    single { MachineViewModel(storePreferences = get()) }
    single { ServiceViewModel(storePreferences = get()) }
}