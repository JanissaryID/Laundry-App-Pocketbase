package com.aluma.owner.data.employee.remote

import android.util.Log
import com.aluma.owner.data.employee.model.LaundryEmployee
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import kotlinx.serialization.json.Json

class EmployeeRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : EmployeeRemoteRepository {

    private val collection = "LaundryEmployee"
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun fetchEmployees(ownerId: String): List<LaundryEmployee> {
        return try {
            client.records.getList<LaundryEmployee>(
                collection,
                page = 1,
                perPage = 100,
                filterBy = io.github.agrevster.pocketbaseKotlin.dsl.query.Filter("user=\"$ownerId\"")
            ).items.reversed()
        } catch (e: Exception) {
            Log.e("EmployeeRepo", "❌ fetchEmployees failed", e)
            emptyList()
        }
    }

    override suspend fun addEmployee(employee: LaundryEmployee) {
        try {
            val body = json.encodeToString(LaundryEmployee.serializer(), employee)
            client.records.create<LaundryEmployee>(collection, body)
        } catch (e: Exception) {
            Log.e("EmployeeRepo", "❌ addEmployee failed", e)
            throw e
        }
    }

    override suspend fun editEmployee(employee: LaundryEmployee, employeeId: String) {
        try {
            val body = json.encodeToString(LaundryEmployee.serializer(), employee)
            client.records.update<LaundryEmployee>(
                sub = collection,
                id = employeeId,
                body = body
            )
        } catch (e: Exception) {
            Log.e("EmployeeRepo", "❌ editEmployee failed", e)
            throw e
        }
    }

    override suspend fun deleteEmployee(employeeId: String) {
        try {
            client.records.delete(collection, employeeId)
        } catch (e: Exception) {
            Log.e("EmployeeRepo", "❌ deleteEmployee failed", e)
            throw e
        }
    }
}
