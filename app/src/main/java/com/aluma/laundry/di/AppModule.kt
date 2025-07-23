package com.aluma.laundry.di

import androidx.room.Room
import com.aluma.laundry.data.AppDatabase
import com.aluma.laundry.data.PocketbaseClientProvider
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.machine.local.MachineRepository
import com.aluma.laundry.data.machine.remote.MachineRemoteRepository
import com.aluma.laundry.data.machine.remote.MachineRemoteRepositoryImpl
import com.aluma.laundry.data.machine.remote.MachineRemoteViewModel
import com.aluma.laundry.data.order.local.OrderLocalViewModel
import com.aluma.laundry.data.order.local.OrderRepository
import com.aluma.laundry.data.order.remote.OrderRemoteRepository
import com.aluma.laundry.data.order.remote.OrderRemoteRepositoryImpl
import com.aluma.laundry.data.order.remote.OrderRemoteViewModel
import com.aluma.laundry.data.service.remote.ServiceRemoteRepository
import com.aluma.laundry.data.service.remote.ServiceRemoteRepositoryImpl
import com.aluma.laundry.data.service.remote.ServiceRemoteViewModel
import com.aluma.laundry.data.store.StoreRemoteRepository
import com.aluma.laundry.data.store.StoreRemoteRepositoryImpl
import com.aluma.laundry.data.store.StoreRemoteViewModel
import com.aluma.laundry.data.user.remote.UserRemoteRepository
import com.aluma.laundry.data.user.remote.UserRemoteRepositoryImpl
import com.aluma.laundry.data.user.remote.UserRemoteViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Data Store
    single { StorePreferences(androidContext()) }
    single { StorePreferenceViewModel(storePreferences = get()) }

    // Pocketbase Client
    single { PocketbaseClientProvider.client }

    // Repository
    single<OrderRemoteRepository> { OrderRemoteRepositoryImpl(get()) }
    single<UserRemoteRepository> { UserRemoteRepositoryImpl(get()) }
    single<StoreRemoteRepository> { StoreRemoteRepositoryImpl(get()) }
    single<MachineRemoteRepository> { MachineRemoteRepositoryImpl(get()) }
    single<ServiceRemoteRepository> { ServiceRemoteRepositoryImpl(get()) }

    // ViewModel
    viewModel {
        OrderRemoteViewModel(
            storePreferences = get(),
            orderRepository = get(),
            client = get()
        )
    }

    viewModel {
        UserRemoteViewModel(
            storePreferences = get(),
            userRepository = get()
        )
    }

    viewModel {
        StoreRemoteViewModel(
            storePreferences = get(),
            storeRepository = get(),
            client = get()
        )
    }

    viewModel {
        ServiceRemoteViewModel(
            storePreferences = get(),
            serviceRepository = get(),
            client = get()
        )
    }

    viewModel {
        MachineRemoteViewModel(
            storePreferences = get(),
            machineRepository = get(),
            machineLocalViewModel = get(),
            client = get()
        )
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "laundry_database"
        ).fallbackToDestructiveMigration(false).build()
    }

    single { get<AppDatabase>().machineDao() }
    single { get<AppDatabase>().orderDao() }

    // Repositories
    single { MachineRepository(get()) }
    single { OrderRepository(get()) }

    // Local ViewModels
    single { MachineLocalViewModel(get()) }
    single { OrderLocalViewModel(get(), get()) }
}