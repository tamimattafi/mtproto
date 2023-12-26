package com.attafitamim.mtproto.client.sample

class JVMPlatform: Platform {
    override val name: String = "JVM"
}

actual fun getPlatform(): Platform = JVMPlatform()