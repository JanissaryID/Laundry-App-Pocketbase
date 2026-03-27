package com.aluma.laundry.di

import androidx.activity.ComponentActivity
import androidx.room.Room
import androidx.work.WorkManager
import com.aluma.laundry.bluetooth.BluetoothHelper
import com.aluma.laundry.data.AppDatabase
import com.aluma.laundry.data.PocketbaseClientProvider
import com.aluma.laundry.data.datastore.StorePreferenceViewModel
import com.aluma.laundry.data.datastore.StorePreferences
import com.aluma.laundry.data.income.remote.IncomeRemoteRepository
import com.aluma.laundry.data.income.remote.IncomeRemoteRepositoryImpl
import com.aluma.laundry.data.income.remote.IncomeRemoteViewModel
import com.aluma.laundry.data.logmachine.local.LogMachineLocalRepository
import com.aluma.laundry.data.logmachine.local.LogMachineLocalViewModel
import com.aluma.laundry.data.logmachine.remote.LogMachineRemoteRepository
import com.aluma.laundry.data.logmachine.remote.LogMachineRemoteRepositoryImpl
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
import com.aluma.laundry.data.employee.remote.EmployeeRemoteRepository
import com.aluma.laundry.data.employee.remote.EmployeeRemoteRepositoryImpl
import com.aluma.laundry.data.employee.remote.EmployeeRemoteViewModel
import com.aluma.laundry.data.attendance.remote.AttendanceRemoteRepository
import com.aluma.laundry.data.attendance.remote.AttendanceRemoteRepositoryImpl
import com.aluma.laundry.data.attendance.remote.AttendanceRemoteViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val workManagerModule = module {
    single { WorkManager.getInstance(androidContext()) }
}

val appModule = module {
    // Bluetooth
    factory { (activity: ComponentActivity) -> BluetoothHelper(activity) }
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
    single<EmployeeRemoteRepository> { EmployeeRemoteRepositoryImpl(get()) }
    single<AttendanceRemoteRepository> { AttendanceRemoteRepositoryImpl(get()) }

    // ViewModel
    viewModel {
        IncomeRemoteViewModel(
            storePreferences = get(),
            client = get(),
            incomeRemoteRepository = get()
        )
    }

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

    viewModel {
        EmployeeRemoteViewModel(
            storePreferences = get(),
            client = get(),
            employeeRepository = get()
        )
    }

    viewModel {
        AttendanceRemoteViewModel(
            storePreferences = get(),
            client = get(),
            attendanceRepository = get()
        )
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "laundry_database"
        ).fallbackToDestructiveMigration(true).build()
    }

    single { get<AppDatabase>().machineDao() }
    single { get<AppDatabase>().orderDao() }
    single { get<AppDatabase>().serviceDao() }
    single { get<AppDatabase>().logMachineDao() }

    // Repositories
    single { MachineLocalRepository(get()) }
    single { OrderLocalRepository(get()) }
    single { ServiceLocalRepository(get()) }
    single { LogMachineLocalRepository(get()) }

    // Local ViewModels
    single { MachineLocalViewModel(get()) }
    single { ServiceLocalViewModel(get()) }
    single { LogMachineLocalViewModel(get()) }
    single { OrderLocalViewModel(
        repo =get(),
        machineRepo = get(),
        client = get(),
        storePreferences = get(),
        logMachineLocalRepository = get(),
        logMachineRemoteRepository = get(),
        incomeRemoteRepository = get(),
        orderRemoteRepository = get())
    }
}