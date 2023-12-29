package com.attafitamim.mtproto.client.sample

import com.russhwolf.settings.NSUserDefaultsSettings
import kotlinx.coroutines.launch
import platform.Foundation.NSUserDefaults

class ConnectionManager {

    fun start() {
        val defaults = NSUserDefaults(suiteName = "mtproto")
        val settings = NSUserDefaultsSettings(defaults)

        ConnectionHelper.scope.launch {
            Playground.initConnection(settings)
        }
    }
}