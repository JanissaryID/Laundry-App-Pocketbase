package com.aluma.laundry.data.employee.remote

import com.aluma.laundry.data.employee.model.EmployeeRemote

interface EmployeeRemoteRepository {
    suspend fun fetchEmployees(userID: String, storeID: String? = null): List<EmployeeRemote>
}
