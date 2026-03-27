package com.aluma.owner.di

import androidx.room.Room
import com.aluma.owner.data.AppDatabase
import com.aluma.owner.data.PocketbaseClientProvider
import com.aluma.owner.data.datastore.StorePreferenceViewModel
import com.aluma.owner.data.datastore.StorePreferences
import com.aluma.owner.data.attendance.remote.AttendanceRemoteRepository
import com.aluma.owner.data.attendance.remote.AttendanceRemoteRepositoryImpl
import com.aluma.owner.data.attendance.remote.AttendanceRemoteViewModel
import com.aluma.owner.data.employee.remote.EmployeeRemoteRepository
import com.aluma.owner.data.employee.remote.EmployeeRemoteRepositoryImpl
import com.aluma.owner.data.employee.remote.EmployeeRemoteViewModel
import com.aluma.owner.data.income.remote.IncomeRemoteRepository
import com.aluma.owner.data.income.remote.IncomeRemoteRepositoryImpl
import com.aluma.owner.data.income.remote.IncomeRemoteViewModel
import com.aluma.owner.data.logmachine.remote.LogMachineRemoteRepository
import com.aluma.owner.data.logmachine.remote.LogMachineRemoteRepositoryImpl
import com.aluma.owner.data.logmachine.remote.LogMachineRemoteViewModel
import com.aluma.owner.data.machine.remote.MachineRemoteRepository
import com.aluma.owner.data.machine.remote.MachineRemoteRepositoryImpl
import com.aluma.owner.data.machine.remote.MachineRemoteViewModel
import com.aluma.owner.data.order.remote.OrderRemoteRepository
import com.aluma.owner.data.order.remote.OrderRemoteRepositoryImpl
import com.aluma.owner.data.order.remote.OrderRemoteViewModel
import com.aluma.owner.data.service.remote.ServiceRemoteRepository
import com.aluma.owner.data.service.remote.ServiceRemoteRepositoryImpl
import com.aluma.owner.data.service.remote.ServiceRemoteViewModel
import com.aluma.owner.data.store.local.StoreLocalRepository
import com.aluma.owner.data.store.local.StoreLocalViewModel
import com.aluma.owner.data.store.remote.StoreRemoteRepository
import com.aluma.owner.data.store.remote.StoreRemoteRepositoryImpl
import com.aluma.owner.data.store.remote.StoreRemoteViewModel
import com.aluma.owner.data.user.remote.UserRemoteRepository
import com.aluma.owner.data.user.remote.UserRemoteRepositoryImpl
import com.aluma.owner.data.user.remote.UserRemoteViewModel
import com.aluma.owner.data.realtime.RealtimeRepository
import com.aluma.owner.data.realtime.RealtimeRepositoryImpl
import com.aluma.owner.data.realtime.RealtimeViewModel
import com.aluma.owner.utils.ExcelPOIViewModel
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
    single<AttendanceRemoteRepository> { AttendanceRemoteRepositoryImpl(get()) }
    single<EmployeeRemoteRepository> { EmployeeRemoteRepositoryImpl(get()) }
    single<RealtimeRepository> { RealtimeRepositoryImpl(get()) }

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
        LogMachineRemoteViewModel(
            logMachineRemoteRepository = get(),
            client = get(),
            storePreferences = get()
        )
    }

    single {
        EmployeeRemoteViewModel(
            employeeRepository = get(),
            attendanceRepository = get(),
            client = get(),
            storePreferences = get()
        )
    }

    single {
        AttendanceRemoteViewModel(
            attendanceRepository = get()
        )
    }

    single {
        ExcelPOIViewModel()
    }

    single {
        RealtimeViewModel(
            storePreferences = get(),
            realtimeRepository = get(),
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

    single { get<AppDatabase>().storeDao() }
    single { StoreLocalRepository(get()) }
    single { StoreLocalViewModel(get()) }

}