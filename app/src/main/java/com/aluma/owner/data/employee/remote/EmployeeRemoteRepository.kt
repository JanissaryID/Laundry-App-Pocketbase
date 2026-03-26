package com.aluma.owner.data.employee.remote

import com.aluma.owner.data.employee.model.LaundryEmployee

interface EmployeeRemoteRepository {
    suspend fun fetchEmployees(ownerId: String): List<LaundryEmployee>
    suspend fun addEmployee(employee: LaundryEmployee)
    suspend fun editEmployee(employee: LaundryEmployee, employeeId: String)
    suspend fun deleteEmployee(employeeId: String)
}
