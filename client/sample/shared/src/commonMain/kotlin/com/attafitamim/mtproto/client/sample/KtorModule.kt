package com.attafitamim.mtproto.client.sample

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import kotlin.time.Duration.Companion.seconds

expect fun createHttpClient(onConfig: HttpClientConfig<*>.() -> Unit): HttpClient

object KtorModule {

    const val TIMEOUT = 5000
    const val PING_INTERVAL = 2

    fun provideHttpClient(): HttpClient = createHttpClient {
        expectSuccess = true
        install(HttpRedirect)

        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT.seconds.inWholeMilliseconds
        }

        install(WebSockets) {
            pingInterval = PING_INTERVAL.seconds.inWholeMilliseconds
        }
    }
}
