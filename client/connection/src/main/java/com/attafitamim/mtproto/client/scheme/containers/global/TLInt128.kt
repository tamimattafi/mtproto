package com.attafitamim.mtproto.client.scheme.containers.global

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLContainer

/**
 * int128 bytes:bytes[16] = Int128;
 */
public data class TLInt128(
  public val bytes: ByteArray,
) : TLContainer {
  override fun serialize(outputStream: TLOutputStream) {
    require(bytes.size == 16)
    outputStream.writeByteArray(bytes)
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLInt128 {
      val bytes: ByteArray = inputStream.readBytes(16)
      return TLInt128(bytes)
    }
  }
}
