package com.aluma.laundry.di

import androidx.room.Room
import com.aluma.laundry.data.AppDatabase
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.machine.local.MachineRepository
import com.aluma.laundry.data.machine.remote.MachineViewModel
import com.aluma.laundry.data.order.local.OrderLocalViewModel
import com.aluma.laundry.data.order.local.OrderRepository
import com.aluma.laundry.data.order.remote.OrderViewModel
import com.aluma.laundry.data.service.remote.ServiceViewModel
import com.aluma.laundry.data.store.StoreViewModel
import com.aluma.laundry.data.user.UserViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    // Data Store
    single { StorePreferences(androidContext()) }

    // Api
    single { UserViewModel(storePreferences = get()) }
    single { StorePreferenceViewModel(storePreferences = get()) }
    single { StoreViewModel(storePreferences = get()) }
    single { MachineViewModel(storePreferences = get(), machineLocalViewModel = get()) }
    single { ServiceViewModel(storePreferences = get()) }
    single { OrderViewModel(storePreferences = get()) }

    // Room Database
    single { Room.databaseBuilder(
        androidContext(),
        AppDatabase::class.java,
        "laundry_database"
        ).fallbackToDestructiveMigration(false).build()
    }
    single { get<AppDatabase>().machineDao() }
    single { get<AppDatabase>().orderDao() }

    single { MachineRepository(get()) }
    single { OrderRepository(get()) }

    single { MachineLocalViewModel(get()) }
    single { OrderLocalViewModel(get(), get()) }
}