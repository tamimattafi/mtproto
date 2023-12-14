package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.types.global.TLDestroySessionRes
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * destroy_session#e7512126 session_id:long = DestroySessionRes;
 */
public data class TLDestroySession(
  public val sessionId: Long,
) : TLMethod<TLDestroySessionRes> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    outputStream.writeLong(sessionId)
  }

  override fun parse(inputStream: TLInputStream): TLDestroySessionRes {
    val response: TLDestroySessionRes = TLDestroySessionRes.parse(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = -414_113_498
  }
}
