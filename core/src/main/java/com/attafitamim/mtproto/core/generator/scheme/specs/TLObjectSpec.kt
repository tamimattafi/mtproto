package com.attafitamim.mtproto.core.generator.scheme.specs

data class TLObjectSpec(
    val rawScheme: String,
    var name: String,
    var namespace: String?,
    val superType: TLTypeSpec.Object,
    val constructorHash: Int,
    val hasFlags: Boolean,
    val propertiesSpecs: List<TLPropertySpec>?,
    val genericVariables: Map<String, TLTypeSpec.Generic.Variable>?
)