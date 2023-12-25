package com.attafitamim.mtproto.sample.shared

import com.attafitamim.mtproto.sample.shared.KtorModule.TIMEOUT
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import kotlin.time.Duration.Companion.seconds
import platform.Foundation.NSTimeInterval

actual fun createHttpClient(onConfig: HttpClientConfig<*>.() -> Unit) = HttpClient(Darwin) {
    engine {
        configureSession {
            val interval = NSTimeInterval
                .fromBits(TIMEOUT.seconds.inWholeMilliseconds.toDouble().toRawBits())

            timeoutIntervalForRequest = interval
            timeoutIntervalForResource = interval
        }
    }

    onConfig()
}
