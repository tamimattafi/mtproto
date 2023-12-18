package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.client.scheme.types.global.TLSetClientDHParamsAnswer
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * set_client_DH_params#f5045f1f nonce:int128 server_nonce:int128 encrypted_data:bytes =
 * Set_client_DH_params_answer;
 */
public data class TLSetClientDHParams(
  public val nonce: TLInt128,
  public val serverNonce: TLInt128,
  public val encryptedData: ByteArray,
) : TLMethod<TLSetClientDHParamsAnswer> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    nonce.serialize(outputStream)
    serverNonce.serialize(outputStream)
    outputStream.writeWrappedByteArray(encryptedData)
  }

  override fun parse(inputStream: TLInputStream): TLSetClientDHParamsAnswer {
    val response: TLSetClientDHParamsAnswer = TLSetClientDHParamsAnswer.parse(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = -184_262_881
  }
}
