package com.attafitamim.mtproto.client.sample

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import kotlin.time.Duration.Companion.seconds

expect fun createHttpClient(onConfig: HttpClientConfig<*>.() -> Unit): HttpClient

object KtorModule {

    const val TIMEOUT = 5000
    const val PING_INTERVAL = 2

    fun provideHttpClient(): HttpClient = createHttpClient {
        expectSuccess = true
        install(HttpRedirect)

        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    println("Socket: $message")
                }
            }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT.seconds.inWholeMilliseconds
        }

        install(WebSockets) {
            pingInterval = PING_INTERVAL.seconds.inWholeMilliseconds
        }
    }
}
