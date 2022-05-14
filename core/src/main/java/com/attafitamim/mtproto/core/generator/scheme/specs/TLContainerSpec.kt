package com.attafitamim.mtproto.core.generator.scheme.specs

data class TLContainerSpec(
    val rawScheme: String,
    var name: String,
    var namespace: String?,
    val superType: TLTypeSpec.TLType.Container,
    val hasFlags: Boolean,
    val propertiesSpecs: List<TLPropertySpec>,
    val genericVariables: Map<String, TLTypeSpec.Generic.Variable>?
)
