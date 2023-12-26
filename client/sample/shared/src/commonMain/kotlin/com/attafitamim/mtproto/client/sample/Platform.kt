package com.attafitamim.mtproto.client.sample

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform