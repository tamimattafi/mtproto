package com.attafitamim.mtproto.generator.scheme.specs

data class TLMethodSpec(
    val rawScheme: String,
    var name: String,
    var namespace: String?,
    val returnType: TLTypeSpec,
    val constructorHash: String,
    val hasFlags: Boolean,
    val propertiesSpecs: List<TLPropertySpec>?,
    val genericVariables: Map<String, TLTypeSpec.Generic.Variable>?
)
