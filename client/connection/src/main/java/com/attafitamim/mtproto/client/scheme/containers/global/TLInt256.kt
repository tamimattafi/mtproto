package com.attafitamim.mtproto.client.scheme.containers.global

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLContainer

/**
 * int256 bytes:bytes[32] = Int256;
 */
public data class TLInt256(
  public val bytes: ByteArray,
) : TLContainer {
  override fun serialize(outputStream: TLOutputStream) {
    require(bytes.size == 32)
    outputStream.writeByteArray(bytes)
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLInt256 {
      val bytes: ByteArray = inputStream.readBytes(32)
      return TLInt256(bytes)
    }
  }
}
