package com.attafitamim.mtproto.core.generator.classes

object Constants {
    //TODO: fix hard code, use plugin input
    const val BASE_PACKAGE_NAME = "com.tezro.messenger"
    const val PACKAGE_SEPARATOR = "."
    const val TYPES_FOLDER_NAME = "types"
    const val METHODS_FOLDER_NAME = "methods"
    const val GLOBAL_DATA_TYPES_FOLDER_NAME = "global"

    const val TYPES_PREFIX = "TL"

    const val TL_VECTOR_TYPE_NAME = "vector"
    const val TL_VECTOR_TYPE_CONSTRUCTOR = 0x1cb5c415

    const val OBJECT_NAME_SPACE_SEPARATOR = "."

    const val PARSE_METHOD_NAME = "parse"
    const val PARSE_RESPONSE_METHOD_NAME = "parseResponse"
    const val SERIALIZE_METHOD_NAME = "serialize"

    const val BUFFER_PARAMETER_NAME = "buffer"
    const val EXCEPTION_PARAMETER_NAME = "exception"

    const val FLAGS_PROPERTY_NAME = "flags"
}