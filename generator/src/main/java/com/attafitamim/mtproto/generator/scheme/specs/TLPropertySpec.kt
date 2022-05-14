package com.attafitamim.mtproto.generator.scheme.specs

data class TLPropertySpec(
    val rawScheme: String,
    val name: String,
    val flag: Int?,
    val typeSpec: TLTypeSpec
)
