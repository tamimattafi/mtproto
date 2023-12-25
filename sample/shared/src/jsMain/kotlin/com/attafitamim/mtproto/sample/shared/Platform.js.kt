package com.attafitamim.mtproto.sample.shared

class WebPlatform: Platform {
    override val name: String = "Web"
}

actual fun getPlatform(): Platform = WebPlatform()