package com.attafitamim.mtproto.sample

import com.attafitamim.mtproto.sample.shared.ConnectionHelper
import com.attafitamim.mtproto.sample.shared.Playground
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun main() {
    val preferences = Preferences.userNodeForPackage(ConnectionHelper::class.java)
    val settings = PreferencesSettings(preferences)

    ConnectionHelper.scope.launch {
        Playground.initConnection(settings)
    }

    while (ConnectionHelper.scope.isActive) {
        Thread.sleep(10000)
    }
}