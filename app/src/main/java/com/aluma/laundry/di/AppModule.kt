package com.aluma.laundry.di

import androidx.room.Room
import com.aluma.laundry.data.AppDatabase
import com.aluma.laundry.data.PocketbaseClientProvider
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.income.remote.IncomeRemoteRepository
import com.aluma.laundry.data.income.remote.IncomeRemoteRepositoryImpl
import com.aluma.laundry.data.income.remote.IncomeRemoteViewModel
import com.aluma.laundry.data.logmachine.remote.LogMachineRemoteRepository
import com.aluma.laundry.data.logmachine.remote.LogMachineRemoteRepositoryImpl
import com.aluma.laundry.data.machine.remote.MachineRemoteRepository
import com.aluma.laundry.data.machine.remote.MachineRemoteRepositoryImpl
import com.aluma.laundry.data.machine.remote.MachineRemoteViewModel
import com.aluma.laundry.data.order.remote.OrderRemoteRepository
import com.aluma.laundry.data.order.remote.OrderRemoteRepositoryImpl
import com.aluma.laundry.data.order.remote.OrderRemoteViewModel
import com.aluma.laundry.data.service.remote.ServiceRemoteRepository
import com.aluma.laundry.data.service.remote.ServiceRemoteRepositoryImpl
import com.aluma.laundry.data.service.remote.ServiceRemoteViewModel
import com.aluma.laundry.data.store.local.StoreLocalRepository
import com.aluma.laundry.data.store.local.StoreLocalViewModel
import com.aluma.laundry.data.store.remote.StoreRemoteRepository
import com.aluma.laundry.data.store.remote.StoreRemoteRepositoryImpl
import com.aluma.laundry.data.store.remote.StoreRemoteViewModel
import com.aluma.laundry.data.user.remote.UserRemoteRepository
import com.aluma.laundry.data.user.remote.UserRemoteRepositoryImpl
import com.aluma.laundry.data.user.remote.UserRemoteViewModel
import org.koin.android.ext.koin.androidContext
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
    single<LogMachineRemoteRepository> { LogMachineRemoteRepositoryImpl(get()) }
    single<IncomeRemoteRepository> { IncomeRemoteRepositoryImpl(get()) }

    // ViewModel
    single {
        OrderRemoteViewModel(
            storePreferences = get(),
            orderRepository = get(),
            client = get(),
        )
    }

    single {
        UserRemoteViewModel(
            storePreferences = get(),
            userRepository = get()
        )
    }

    single {
        StoreRemoteViewModel(
            storePreferences = get(),
            storeRepository = get(),
            client = get(),
            storeLocalRepository = get()
        )
    }

    single {
        ServiceRemoteViewModel(
            storePreferences = get(),
            serviceRepository = get(),
            client = get(),
        )
    }

    single {
        MachineRemoteViewModel(
            storePreferences = get(),
            machineRepository = get(),
            client = get()
        )
    }

    single {
        IncomeRemoteViewModel(
            storePreferences = get(),
            incomeRemoteRepository = get(),
            client = get(),
            storeRemoteRepository = get()
        )
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "laundry_database"
        ).fallbackToDestructiveMigration(false).build()
    }

    single { get<AppDatabase>().storeDao() }
    single { StoreLocalRepository(get()) }
    single { StoreLocalViewModel(get()) }

}