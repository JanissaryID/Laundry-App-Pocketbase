package com.aluma.laundry.data

import io.github.agrevster.pocketbaseKotlin.PocketbaseClient
import io.ktor.http.URLProtocol

object PocketbaseClientProvider {
    val client by lazy {
        PocketbaseClient(
            baseUrl = {
                protocol = URLProtocol.HTTPS
                host = "011a1fd1ccb6.ngrok-free.app"
            }
        )
    }
}