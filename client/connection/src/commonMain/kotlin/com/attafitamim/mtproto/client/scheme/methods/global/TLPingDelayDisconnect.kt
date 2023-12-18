package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.types.global.TLPong
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * ping_delay_disconnect#f3427b8c ping_id:long disconnect_delay:int = Pong;
 */
public data class TLPingDelayDisconnect(
  public val pingId: Long,
  public val disconnectDelay: Int,
) : TLMethod<TLPong> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    outputStream.writeLong(pingId)
    outputStream.writeInt(disconnectDelay)
  }

  override fun parse(inputStream: TLInputStream): TLPong {
    val response: TLPong = TLPong.parse(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = -213_746_804
  }
}
