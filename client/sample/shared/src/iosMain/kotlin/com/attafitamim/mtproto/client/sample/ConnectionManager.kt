package com.attafitamim.mtproto.client.sample

import com.russhwolf.settings.NSUserDefaultsSettings
import kotlinx.coroutines.launch
import platform.Foundation.NSUserDefaults

class ConnectionManager {

    fun start() {
        println("CONNECTION: creating settings")

        val defaults = NSUserDefaults(suiteName = "mtproto")
        val settings = NSUserDefaultsSettings(defaults)

        println("CONNECTION: launching coroutine")
        ConnectionHelper.scope.launch {
            println("CONNECTION: connecting")
            Playground.initConnection(settings)
        }
    }
}