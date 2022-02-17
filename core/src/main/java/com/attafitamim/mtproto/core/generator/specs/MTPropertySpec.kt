package com.attafitamim.mtproto.core.generator.specs

data class MTPropertySpec(
    val rawScheme: String,
    val name: String,
    val flag: Int?,
    val typeSpec: MTTypeSpec
)
