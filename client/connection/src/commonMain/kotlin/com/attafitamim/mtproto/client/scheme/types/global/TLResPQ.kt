package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLResPQ : TLObject {
  /**
   * resPQ#05162463 nonce:int128 server_nonce:int128 pq:bytes
   * server_public_key_fingerprints:Vector<long> = ResPQ;
   */
  public data class ResPQ(
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val pq: ByteArray,
    public val serverPublicKeyFingerprints: TLVector<Long>,
  ) : TLResPQ {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      outputStream.writeWrappedByteArray(pq)
      serverPublicKeyFingerprints.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 85_337_187

      public fun parse(inputStream: TLInputStream): ResPQ {
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val pq: ByteArray = inputStream.readWrappedBytes()
        val serverPublicKeyFingerprints: TLVector<Long> = TLVector.parse(inputStream, {
            it.readLong() })
        return ResPQ(nonce, serverNonce, pq, serverPublicKeyFingerprints)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLResPQ {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        ResPQ.CONSTRUCTOR_HASH -> ResPQ.parse(inputStream)
        else -> throw TLObjectParseException(TLResPQ::class, constructorHash)
      }
    }
  }
}
