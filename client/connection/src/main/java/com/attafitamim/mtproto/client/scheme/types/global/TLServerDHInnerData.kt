package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLServerDHInnerData : TLObject {
  /**
   * server_DH_inner_data#b5890dba nonce:int128 server_nonce:int128 g:int dh_prime:bytes g_a:bytes
   * server_time:int = Server_DH_inner_data;
   */
  public data class ServerDHInnerData(
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val g: Int,
    public val dhPrime: ByteArray,
    public val gA: ByteArray,
    public val serverTime: Int,
  ) : TLServerDHInnerData {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      outputStream.writeInt(g)
      outputStream.writeWrappedByteArray(dhPrime)
      outputStream.writeWrappedByteArray(gA)
      outputStream.writeInt(serverTime)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -1_249_309_254

      public fun parse(inputStream: TLInputStream): ServerDHInnerData {
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val g: Int = inputStream.readInt()
        val dhPrime: ByteArray = inputStream.readWrappedBytes()
        val gA: ByteArray = inputStream.readWrappedBytes()
        val serverTime: Int = inputStream.readInt()
        return ServerDHInnerData(nonce, serverNonce, g, dhPrime, gA, serverTime)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLServerDHInnerData {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        ServerDHInnerData.CONSTRUCTOR_HASH -> ServerDHInnerData.parse(inputStream)
        else -> throw TLObjectParseException(TLServerDHInnerData::class, constructorHash)
      }
    }
  }
}
