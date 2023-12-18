package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.core.serialization.helpers.SerializationHelper
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * invokeAfterMsg#cb9f372d {X:Type} msg_id:long query:X = X;
 */
public data class TLInvokeAfterMsg<X : Any>(
  public val msgId: Long,
  public val query: X,
  public val parseX: (inputStream: TLInputStream) -> X,
) : TLMethod<X> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    outputStream.writeLong(msgId)
    SerializationHelper.serialize(outputStream, query)
  }

  override fun parse(inputStream: TLInputStream): X {
    val response: X = parseX.invoke(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = -878_758_099
  }
}
