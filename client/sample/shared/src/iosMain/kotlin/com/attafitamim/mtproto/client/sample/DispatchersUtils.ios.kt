package com.attafitamim.mtproto.client.sample

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

actual val Dispatchers.IO: CoroutineDispatcher get() = IO
