package com.attafitamim.mtproto.generator.scheme.specs

import kotlin.reflect.KClass

sealed interface TLTypeSpec {

    object Type : TLTypeSpec

    object Flag : TLTypeSpec

    data class Primitive(
        val clazz: KClass<out Any>
    ) : TLTypeSpec

    sealed interface TLType : TLTypeSpec {

        object SuperObject : TLType

        object SuperContainer : TLType

        data class Object(
            val namespace: String?,
            val name: String,
            val generics: List<Generic>?
        ) : TLType

        data class Container(
            val namespace: String?,
            val name: String,
            val generics: List<Generic>?
        ) : TLType
    }
    
    sealed interface Structure : TLTypeSpec {

        data class Collection(
            val clazz: KClass<out Any>,
            val elementGeneric: Generic
        ) : Structure

        data class Bytes(
            val fixedSize: Int?
        ) : Structure
    }

    sealed interface Generic : TLTypeSpec {

        data class Variable(
            val name: String,
            val superType: TLTypeSpec,
        ) : Generic

        data class Parameter(
            val type: TLTypeSpec
        ) : Generic
    }
}
