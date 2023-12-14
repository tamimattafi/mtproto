package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.types.global.TLPong
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * ping#7abe77ec ping_id:long = Pong;
 */
public data class TLPing(
  public val pingId: Long,
) : TLMethod<TLPong> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    outputStream.writeLong(pingId)
  }

  override fun parse(inputStream: TLInputStream): TLPong {
    val response: TLPong = TLPong.parse(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = 2_059_302_892
  }
}
