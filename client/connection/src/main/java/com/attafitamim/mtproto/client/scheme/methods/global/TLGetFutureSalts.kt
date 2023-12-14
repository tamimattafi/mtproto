package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.types.global.TLFutureSalts
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * get_future_salts#b921bd04 num:int = FutureSalts;
 */
public data class TLGetFutureSalts(
  public val num: Int,
) : TLMethod<TLFutureSalts> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    outputStream.writeInt(num)
  }

  override fun parse(inputStream: TLInputStream): TLFutureSalts {
    val response: TLFutureSalts = TLFutureSalts.parse(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = -1_188_971_260
  }
}
