package com.attafitamim.mtproto.sample.shared

class JVMPlatform: Platform {
    override val name: String = "JVM"
}

actual fun getPlatform(): Platform = JVMPlatform()