package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.types.global.TLVector
import com.attafitamim.mtproto.core.serialization.helpers.SerializationHelper
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * invokeAfterMsgs#3dc4b4f0 {X:Type} msg_ids:Vector<long> query:X = X;
 */
public data class TLInvokeAfterMsgs<X : Any>(
  public val msgIds: TLVector<Long>,
  public val query: X,
  public val parseX: (inputStream: TLInputStream) -> X,
) : TLMethod<X> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    msgIds.serialize(outputStream)
    SerializationHelper.serialize(outputStream, query)
  }

  override fun parse(inputStream: TLInputStream): X {
    val response: X = parseX.invoke(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = 1_036_301_552
  }
}
