package com.attafitamim.mtproto.client.connection.manager

import com.attafitamim.mtproto.client.api.connection.IConnection
import com.attafitamim.mtproto.security.obfuscation.IObfuscator

class ObfuscatedConnection(
    val connection: IConnection,
    val obfuscator: IObfuscator
)
