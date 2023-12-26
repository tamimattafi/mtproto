package com.attafitamim.mtproto.client.sample

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual val Dispatchers.IO: CoroutineDispatcher get() = IO
