package com.attafitamim.mtproto.core.generator.scheme.specs

data class TLMethodSpec(
    val rawScheme: String,
    var name: String,
    var namespace: String?,
    val returnType: TLTypeSpec,
    val hash: Int,
    val hasFlags: Boolean,
    val propertiesSpecs: List<TLPropertySpec>?,
    val genericVariables: Map<String, TLTypeSpec.Generic.Variable>?
)