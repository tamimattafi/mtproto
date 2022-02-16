package com.attafitamim.mtproto.core.generator.types

data class TLObjectSpecs(
        val rawScheme: String,
        var name: String,
        val superClassName: String,
        val constructor: Int,
        val hasFlags: Boolean,
        val propertiesSpecs: List<TLPropertySpecs>?
)