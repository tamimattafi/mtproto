package com.attafitamim.mtproto.client.sample

import com.attafitamim.mtproto.client.sample.KtorModule.TIMEOUT
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.cio.CIO
import kotlin.time.Duration.Companion.seconds

actual fun createHttpClient(onConfig: HttpClientConfig<*>.() -> Unit) = HttpClient(CIO) {
    engine {
        requestTimeout = TIMEOUT.seconds.inWholeMilliseconds
    }

    onConfig()
}

