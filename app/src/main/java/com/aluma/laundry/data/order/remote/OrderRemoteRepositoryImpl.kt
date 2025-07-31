package com.aluma.laundry.data.order.remote

import com.aluma.laundry.data.order.model.OrderRemote
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
            // Awal dan akhir hari dalam UTC
            val start = it.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS'Z'"))
            val end = it.atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS'Z'"))

            "created >= \"$start\" && created <= \"$end\""
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