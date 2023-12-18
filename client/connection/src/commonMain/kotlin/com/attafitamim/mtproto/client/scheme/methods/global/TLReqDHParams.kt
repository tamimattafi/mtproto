package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.client.scheme.types.global.TLServerDHParams
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * req_DH_params#d712e4be nonce:int128 server_nonce:int128 p:bytes q:bytes
 * public_key_fingerprint:long encrypted_data:bytes = Server_DH_Params;
 */
public data class TLReqDHParams(
  public val nonce: TLInt128,
  public val serverNonce: TLInt128,
  public val p: ByteArray,
  public val q: ByteArray,
  public val publicKeyFingerprint: Long,
  public val encryptedData: ByteArray,
) : TLMethod<TLServerDHParams> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    nonce.serialize(outputStream)
    serverNonce.serialize(outputStream)
    outputStream.writeWrappedByteArray(p)
    outputStream.writeWrappedByteArray(q)
    outputStream.writeLong(publicKeyFingerprint)
    outputStream.writeWrappedByteArray(encryptedData)
  }

  override fun parse(inputStream: TLInputStream): TLServerDHParams {
    val response: TLServerDHParams = TLServerDHParams.parse(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = -686_627_650
  }
}
