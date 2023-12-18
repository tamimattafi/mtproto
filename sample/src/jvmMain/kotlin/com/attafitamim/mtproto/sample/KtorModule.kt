package com.attafitamim.mtproto.sample

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import kotlin.time.Duration.Companion.seconds

object KtorModule {

    private const val TIMEOUT = 5000
    private const val PING_INTERVAL = 2

    fun provideHttpClient(): HttpClient = HttpClient(CIO) {
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

        engine {
            requestTimeout = TIMEOUT.seconds.inWholeMilliseconds
        }

        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT.seconds.inWholeMilliseconds
        }

        install(WebSockets) {
            pingInterval = PING_INTERVAL.seconds.inWholeMilliseconds
        }
    }
}
