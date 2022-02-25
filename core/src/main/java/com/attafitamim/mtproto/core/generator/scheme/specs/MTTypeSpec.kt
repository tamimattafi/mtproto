package com.attafitamim.mtproto.core.generator.scheme.specs

import kotlin.reflect.KClass

sealed interface MTTypeSpec {

    object Type : MTTypeSpec

    data class Primitive(
        val clazz: KClass<out Any>
    ) : MTTypeSpec

    data class Object(
        val namespace: String?,
        val name: String,
        val generics: List<Generic>?
    ) : MTTypeSpec

    sealed interface Structure : MTTypeSpec {

        data class Collection(
            val clazz: KClass<out Any>,
            val elementGeneric: Generic
        ) : Structure
    }

    sealed interface Generic : MTTypeSpec {

        data class Variable(
            val name: String,
            val superType: MTTypeSpec,
        ) : Generic

        data class Parameter(
            val type: MTTypeSpec
        ) : Generic
    }
}