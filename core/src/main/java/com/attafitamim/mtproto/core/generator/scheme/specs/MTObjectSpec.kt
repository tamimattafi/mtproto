package com.attafitamim.mtproto.core.generator.scheme.specs

data class MTObjectSpec(
    val rawScheme: String,
    var name: String,
    var namespace: String?,
    val superType: MTTypeSpec.Object,
    val constructorHash: Int,
    val hasFlags: Boolean,
    val propertiesSpecs: List<MTPropertySpec>?,
    val genericVariables: Map<String, MTTypeSpec.Generic.Variable>?
)