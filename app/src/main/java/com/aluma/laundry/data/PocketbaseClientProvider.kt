package com.aluma.laundry.data

import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.ktor.http.URLProtocol
import com.aluma.laundry.BuildConfig

object PocketbaseClientProvider {
    val client by lazy {
        PocketbaseClient(
            baseUrl = {
                protocol = URLProtocol.HTTPS
                host = BuildConfig.BASE_URL
            }
        )
    }
}