package com.attafitamim.mtproto.client.sockets.test

import com.attafitamim.mtproto.client.sockets.test.helpers.ConnectionHelper
import com.attafitamim.mtproto.client.sockets.test.helpers.Playground
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun main() {
    ConnectionHelper.scope.launch {
        Playground.initConnection()
    }

    while (ConnectionHelper.scope.isActive) {
        Thread.sleep(3000)
    }
}
