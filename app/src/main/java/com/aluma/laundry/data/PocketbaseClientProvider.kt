package com.aluma.laundry.data

import com.aluma.laundry.BuildConfig
import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.ktor.http.URLProtocol

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