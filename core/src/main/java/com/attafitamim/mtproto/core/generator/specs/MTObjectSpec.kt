package com.attafitamim.mtproto.core.generator.specs

data class MTObjectSpec(
    val rawScheme: String,
    var name: String,
    var namespace: String?,
    val superType: MTTypeSpec,
    val hash: Int,
    val hasFlags: Boolean,
    val propertiesSpecs: List<MTPropertySpec>?,
    val genericVariables: Map<String, MTTypeSpec.Generic.Variable>?
)