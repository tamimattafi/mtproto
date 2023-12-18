package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.client.scheme.types.global.TLResPQ
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * req_pq_multi#be7e8ef1 nonce:int128 = ResPQ;
 */
public data class TLReqPqMulti(
  public val nonce: TLInt128,
) : TLMethod<TLResPQ> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    nonce.serialize(outputStream)
  }

  override fun parse(inputStream: TLInputStream): TLResPQ {
    val response: TLResPQ = TLResPQ.parse(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = -1_099_002_127
  }
}
