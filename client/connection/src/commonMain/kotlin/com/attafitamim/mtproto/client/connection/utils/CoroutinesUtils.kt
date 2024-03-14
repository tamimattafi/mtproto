package com.attafitamim.mtproto.client.connection.utils

import kotlinx.coroutines.sync.Mutex

fun Mutex.tryUnlock() {
    if (isLocked) unlock()
}
