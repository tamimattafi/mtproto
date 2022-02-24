package com.attafitamim.mtproto.core.generator.specs

import kotlin.reflect.KClass

sealed interface MTTypeSpec {

    data class Local(
        val clazz: KClass<out Any>
    ) : MTTypeSpec

    sealed interface Structure : MTTypeSpec {

        data class Collection(
            val clazz: KClass<out Any>,
            val elementGeneric: Generic
        ) : Structure
    }

    data class Object(
        val namespace: String?,
        val name: String,
        val generics: List<Generic>?
    ) : MTTypeSpec

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