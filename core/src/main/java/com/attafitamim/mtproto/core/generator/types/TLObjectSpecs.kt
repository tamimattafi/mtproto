package com.attafitamim.mtproto.core.generator.types

data class TLObjectSpecs(
        val rawScheme: String,
        var name: String,
        val superClassName: String,
        val hash: Int,
        val hasFlags: Boolean,
        val propertiesSpecs: List<TLPropertySpecs>?
)