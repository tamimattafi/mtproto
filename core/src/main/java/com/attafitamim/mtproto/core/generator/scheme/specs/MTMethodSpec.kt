package com.attafitamim.mtproto.core.generator.scheme.specs

data class MTMethodSpec(
    val rawScheme: String,
    var name: String,
    var namespace: String?,
    val returnType: MTTypeSpec,
    val hash: Int,
    val hasFlags: Boolean,
    val propertiesSpecs: List<MTPropertySpec>?,
    val genericVariables: Map<String, MTTypeSpec.Generic.Variable>?
)