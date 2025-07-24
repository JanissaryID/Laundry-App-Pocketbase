package com.aluma.laundry.di

import androidx.room.Room
import androidx.work.ListenableWorker
import com.aluma.laundry.data.AppDatabase
import com.aluma.laundry.data.PocketbaseClientProvider
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.machine.local.MachineLocalRepository
import com.aluma.laundry.data.machine.local.MachineLocalViewModel
import com.aluma.laundry.data.machine.remote.MachineRemoteRepository
import com.aluma.laundry.data.machine.remote.MachineRemoteRepositoryImpl
import com.aluma.laundry.data.machine.remote.MachineRemoteViewModel
import com.aluma.laundry.data.order.local.OrderLocalRepository
import com.aluma.laundry.data.order.local.OrderLocalViewModel
import com.aluma.laundry.data.order.remote.OrderRemoteRepository
import com.aluma.laundry.data.order.remote.OrderRemoteRepositoryImpl
import com.aluma.laundry.data.order.remote.OrderRemoteViewModel
import com.aluma.laundry.data.service.local.ServiceLocalRepository
import com.aluma.laundry.data.service.local.ServiceLocalViewModel
import com.aluma.laundry.data.service.remote.ServiceRemoteRepository
import com.aluma.laundry.data.service.remote.ServiceRemoteRepositoryImpl
import com.aluma.laundry.data.service.remote.ServiceRemoteViewModel
import com.aluma.laundry.data.store.StoreRemoteRepository
import com.aluma.laundry.data.store.StoreRemoteRepositoryImpl
import com.aluma.laundry.data.store.StoreRemoteViewModel
import com.aluma.laundry.data.user.remote.UserRemoteRepository
import com.aluma.laundry.data.user.remote.UserRemoteRepositoryImpl
import com.aluma.laundry.data.user.remote.UserRemoteViewModel
import com.aluma.laundry.workmanager.ChildWorkerFactory
import com.aluma.laundry.workmanager.KoinWorkerFactory
import com.aluma.laundry.workmanager.SyncOrderWorker
import com.aluma.laundry.workmanager.SyncOrderWorkerFactory
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val workerModule = module {
    // Bind ChildWorkerFactory
    single<ChildWorkerFactory> {
        SyncOrderWorkerFactory(
            localRepo = get(),
            remoteRepo = get()
        )
    }

    // Worker class map (tanpa javax.inject.Provider)
    single {
        mapOf<Class<out ListenableWorker>, () -> ChildWorkerFactory>(
            SyncOrderWorker::class.java to { get<ChildWorkerFactory>() }
        )
    }

    // Custom Koin WorkerFactory
    single { KoinWorkerFactory(get()) }
}

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
            client = get(),
            appContext = androidContext()
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
            client = get(),
            serviceLocalRepository = get()
        )
    }

    viewModel {
        MachineRemoteViewModel(
            storePreferences = get(),
            machineRepository = get(),
            machineLocalRepository = get(),
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
    single { get<AppDatabase>().serviceDao() }

    // Repositories
    single { MachineLocalRepository(get()) }
    single { OrderLocalRepository(get()) }
    single { ServiceLocalRepository(get()) }

    // Local ViewModels
    single { MachineLocalViewModel(get()) }
    single { ServiceLocalViewModel(get()) }
    single { OrderLocalViewModel(
        repo =get(),
        machineRepo = get(),
        client = get(),
        storePreferences = get(),
        orderRemoteRepository = get())
    }
}