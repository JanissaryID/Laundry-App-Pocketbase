package com.aluma.laundry.data.employee.remote

import com.aluma.laundry.data.employee.model.EmployeeRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter

class EmployeeRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : EmployeeRemoteRepository {

    private val collection = "LaundryEmployee"

    override suspend fun fetchEmployees(userID: String, storeID: String?): List<EmployeeRemote> {
        return client.records.getList<EmployeeRemote>(
            collection,
            page = 1,
            perPage = 100,
            filterBy = if (storeID != null) {
                Filter("user=\"${userID}\" && store=\"${storeID}\"")
            } else {
                Filter("user=\"${userID}\"")
            }
        ).items
    }
}
