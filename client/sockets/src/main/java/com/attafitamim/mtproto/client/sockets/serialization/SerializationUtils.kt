package com.attafitamim.mtproto.client.sockets.serialization

object SerializationUtils {

    // Basic Constructors
    const val BOOLEAN_CONSTRUCTOR_TRUE = 0x997275b5
    const val BOOLEAN_CONSTRUCTOR_FALSE = 0xbc799737

    // Limits
    const val WRAPPED_BYTES_MAX_LENGTH = 254
    const val BYTE_SIZE_DIVISOR = 4

    // Bits
    const val BYTE_BITS_COUNT = 8

    // Slots
    const val BYTE_SLOT_SIZE = 1
    const val SHORT_SLOT_SIZE = BYTE_SLOT_SIZE * 2
    const val INT_SLOT_SIZE = BYTE_SLOT_SIZE * 4
    const val LONG_SLOT_SIZE = BYTE_SLOT_SIZE * 8
}
