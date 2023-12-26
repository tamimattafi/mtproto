package com.attafitamim.mtproto.client.sample

class JSPlatform: Platform {
    override val name: String = "JavaScript"
}

actual fun getPlatform(): Platform = JSPlatform()