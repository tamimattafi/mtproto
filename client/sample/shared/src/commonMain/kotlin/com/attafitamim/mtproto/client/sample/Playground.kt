package com.attafitamim.mtproto.client.sample

import com.attafitamim.mtproto.client.api.connection.ConnectionType
import com.attafitamim.mtproto.client.api.methods.TLHelpGetServerConfig
import com.russhwolf.settings.Settings
import kotlin.random.Random
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Playground {

    suspend fun initConnection(settings: Settings) {
        val connectionManager = ConnectionHelper.createConnectionManager(settings)
        val connectionType = ConnectionType.Generic("calls")

        repeat(5) {
            GlobalScope.launch {
                kotlin.runCatching {
                    delay(Random.nextLong(100, 300))
                    // Generic connection
                    val getConfig = TLHelpGetServerConfig
                    val config = connectionManager.sendRequest(getConfig, connectionType)
                    println("CONNECTION: TLHelpGetServerConfig: $config")
                }.onFailure { error ->
                    println("CONNECTION: TLHelpGetServerConfig: $error")
                }
            }

            GlobalScope.launch {
                kotlin.runCatching {
                    delay(Random.nextLong(100, 300))

                    // Download connection
                    val getConfig = TLHelpGetServerConfig
                    val config = connectionManager.sendRequest(getConfig, ConnectionType.Download)
                    println("TLHelpGetServerConfig: $config")
                }.onFailure { error ->
                    println("CONNECTION: TLHelpGetServerConfig: $error")
                }
            }
        }

        GlobalScope.launch {
            delay(10000L)
            connectionManager.release(resetAuth = false)
        }
    }
}