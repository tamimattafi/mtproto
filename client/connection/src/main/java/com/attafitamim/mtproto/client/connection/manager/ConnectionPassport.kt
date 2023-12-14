package com.attafitamim.mtproto.client.connection.manager

data class ConnectionPassport(
    val apiId: Int,
    val apiHash: String?,
    val deviceModel: String,
    val systemVersion: String,
    val appVersion: String,
    val systemLangCode: String,
    val langPack: String,
    val langCode: String,
    val layer: Int
)