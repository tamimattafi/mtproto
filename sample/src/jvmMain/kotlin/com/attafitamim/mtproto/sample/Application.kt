package com.attafitamim.mtproto.sample

import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun main() {
    ConnectionHelper.scope.launch {
        Playground.initConnection()
    }

    while (ConnectionHelper.scope.isActive) {
        Thread.sleep(10000)
    }
}