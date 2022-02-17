package com.attafitamim.mtproto.core.generator.specs

import kotlin.reflect.KClass

sealed interface MTTypeSpec {

    data class Primitive(
        val clazz: KClass<out Any>
    ) : MTTypeSpec

    data class Local(
        val clazz: KClass<out Any>,
        val generic: Generic?
    ) : MTTypeSpec

    data class Object(
        val namespace: String?,
        val name: String,
        val generic: Generic?
    ) : MTTypeSpec

    sealed class Generic : MTTypeSpec {

        data class Variable(
            val name: String,
            val superType: MTTypeSpec,
        ) : Generic()

        data class Parameter(
            val type: MTTypeSpec
        ) : Generic()
    }
}