package com.aluma.owner.data.order.remote

import com.aluma.owner.data.order.model.OrderRemote
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.github.agrevster.pocketbaseKotlin.dsl.query.Filter
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class OrderRemoteRepositoryImpl(
    private val client: PocketbaseClient
) : OrderRemoteRepository {

    private val collection = "Order"

    override suspend fun fetchOrder(storeID: String, date: LocalDate?): List<OrderRemote> {
        val baseFilter = "store=\"$storeID\""

        val dateFilter = date?.let {
            val dateStr = it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            "date~\"$dateStr\""
        }

        val combinedFilter = listOfNotNull(baseFilter, dateFilter).joinToString(" && ")

        return client.records.getList<OrderRemote>(
            collection,
            page = 1,
            perPage = 200,
            filterBy = Filter(combinedFilter)
        ).items.reversed()
    }
}