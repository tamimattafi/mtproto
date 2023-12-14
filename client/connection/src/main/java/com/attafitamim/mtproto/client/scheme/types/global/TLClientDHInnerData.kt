package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLClientDHInnerData : TLObject {
  /**
   * client_DH_inner_data#6643b654 nonce:int128 server_nonce:int128 retry_id:long g_b:bytes =
   * Client_DH_Inner_Data;
   */
  public data class ClientDHInnerData(
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val retryId: Long,
    public val gB: ByteArray,
  ) : TLClientDHInnerData {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      outputStream.writeLong(retryId)
      outputStream.writeWrappedByteArray(gB)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 1_715_713_620

      public fun parse(inputStream: TLInputStream): ClientDHInnerData {
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val retryId: Long = inputStream.readLong()
        val gB: ByteArray = inputStream.readWrappedBytes()
        return ClientDHInnerData(nonce, serverNonce, retryId, gB)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLClientDHInnerData {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        ClientDHInnerData.CONSTRUCTOR_HASH -> ClientDHInnerData.parse(inputStream)
        else -> throw TLObjectParseException(TLClientDHInnerData::class, constructorHash)
      }
    }
  }
}
