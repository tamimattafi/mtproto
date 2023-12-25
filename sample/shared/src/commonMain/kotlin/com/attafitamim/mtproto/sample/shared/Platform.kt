package com.attafitamim.mtproto.sample.shared

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform