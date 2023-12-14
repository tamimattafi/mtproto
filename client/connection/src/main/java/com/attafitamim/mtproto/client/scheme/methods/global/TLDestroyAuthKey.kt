package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.types.global.TLDestroyAuthKeyRes
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * destroy_auth_key#d1435160 = DestroyAuthKeyRes;
 */
public object TLDestroyAuthKey : TLMethod<TLDestroyAuthKeyRes> {
  public const val CONSTRUCTOR_HASH: Int = -784_117_408

  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
  }

  override fun parse(inputStream: TLInputStream): TLDestroyAuthKeyRes {
    val response: TLDestroyAuthKeyRes = TLDestroyAuthKeyRes.parse(inputStream)
    return response
  }
}
