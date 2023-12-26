package com.attafitamim.mtproto.client.sample

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js

actual fun createHttpClient(onConfig: HttpClientConfig<*>.() -> Unit) = HttpClient(Js, onConfig)