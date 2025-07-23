package com.aluma.laundry.di

import androidx.room.Room
import com.aluma.laundry.data.api.machine.MachineViewModel
import com.aluma.laundry.data.api.order.OrderViewModel
import com.aluma.laundry.data.api.service.ServiceViewModel
import com.aluma.laundry.data.api.store.StoreViewModel
import com.aluma.laundry.data.api.user.UserViewModel
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.room.AppDatabase
import com.aluma.laundry.data.room.machine.MachineRepository
import com.aluma.laundry.data.room.machine.MachineRoomViewModel
import com.aluma.laundry.data.room.order.OrderRepository
import com.aluma.laundry.data.room.order.OrderRoomViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Data Store
    single { StorePreferences(androidContext()) }

    // Api
    single { UserViewModel(storePreferences = get()) }
    single { StorePreferenceViewModel(storePreferences = get()) }
    single { StoreViewModel(storePreferences = get()) }
    single { MachineViewModel(storePreferences = get(), machineRoomViewModel = get()) }
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

    single { MachineRoomViewModel(get()) }
    single { OrderRoomViewModel(get(), get()) }
}