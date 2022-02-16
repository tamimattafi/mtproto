package com.attafitamim.mtproto.generator.classes

object Constants {
    //TODO: fix hard code, use plugin input
    const val BASE_PACKAGE_NAME = "com.tezro.messenger"
    const val PACKAGE_SEPARATOR = "."
    const val TYPES_FOLDER_NAME = "types"
    const val METHODS_FOLDER_NAME = "methods"
    const val GLOBAL_DATA_TYPES_FOLDER_NAME = "global"
    const val CORE_FOLDER_NAME = "core"

    const val TYPES_PREFIX = "TL"

    const val DATA_BUFFER_INTERFACE_NAME = "${TYPES_PREFIX}Buffer"
    const val TL_BASE_OBJECT_NAME = "${TYPES_PREFIX}Object"
    const val TL_BASE_METHOD_NAME = "${TYPES_PREFIX}Method"

    const val TL_VECTOR_TYPE_NAME = "vector"
    const val TL_VECTOR_TYPE_CONSTRUCTOR = 0x1cb5c415

    const val OBJECT_NAME_SPACE_SEPARATOR = "."

    const val PARSE_METHOD_NAME = "parse"
    const val PARSE_RESPONSE_METHOD_NAME = "parseResponse"
    const val SERIALIZE_METHOD_NAME = "serialize"

    const val BUFFER_PARAMETER_NAME = "buffer"
    const val EXCEPTION_PARAMETER_NAME = "exception"

    const val OBJECTS_MAP_PROPERTY_NAME = "objectsMap"
    const val FLAGS_PROPERTY_NAME = "flags"

    const val BUFFER_WRITE_INT_FUNCTION_NAME = "writeInt"
    const val BUFFER_WRITE_LONG_FUNCTION_NAME = "writeLong"
    const val BUFFER_WRITE_BOOLEAN_FUNCTION_NAME = "writeBoolean"
    const val BUFFER_WRITE_BYTES_FUNCTION_NAME = "writeBytes"
    const val BUFFER_WRITE_BYTE_FUNCTION_NAME = "writeByte"
    const val BUFFER_WRITE_STRING_FUNCTION_NAME = "writeString"
    const val BUFFER_WRITE_BYTE_ARRAY_FUNCTION_NAME = "writeByteArray"
    const val BUFFER_WRITE_DOUBLE_FUNCTION_NAME = "writeDouble"
    const val BUFFER_WRITE_BYTE_BUFFER_FUNCTION_NAME = "writeByteBuffer"

    const val BUFFER_READ_INT_FUNCTION_NAME = "readInt"
    const val BUFFER_READ_LONG_FUNCTION_NAME = "readLong"
    const val BUFFER_READ_BOOLEAN_FUNCTION_NAME = "readBoolean"
    const val BUFFER_READ_BYTES_FUNCTION_NAME = "readBytes"
    const val BUFFER_READ_DATA_FUNCTION_NAME = "readData"
    const val BUFFER_READ_BYTE_FUNCTION_NAME = "readByte"
    const val BUFFER_READ_STRING_FUNCTION_NAME = "readString"
    const val BUFFER_READ_BYTE_ARRAY_FUNCTION_NAME = "readByteArray"
    const val BUFFER_READ_DOUBLE_FUNCTION_NAME = "readDouble"
    const val BUFFER_READ_BYTE_BUFFER_FUNCTION_NAME = "readByteBuffer"

    const val BUFFER_GET_SIZE_FUNCTION_NAME = "readByteBuffer"

}