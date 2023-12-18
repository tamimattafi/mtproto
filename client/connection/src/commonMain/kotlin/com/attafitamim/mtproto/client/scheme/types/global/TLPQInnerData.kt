package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.client.scheme.containers.global.TLInt256
import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLPQInnerData : TLObject {
  /**
   * p_q_inner_data#83c95aec pq:bytes p:bytes q:bytes nonce:int128 server_nonce:int128
   * new_nonce:int256 = P_Q_inner_data;
   */
  public data class PQInnerData(
    public val pq: ByteArray,
    public val p: ByteArray,
    public val q: ByteArray,
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val newNonce: TLInt256,
  ) : TLPQInnerData {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeWrappedByteArray(pq)
      outputStream.writeWrappedByteArray(p)
      outputStream.writeWrappedByteArray(q)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      newNonce.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -2_083_955_988

      public fun parse(inputStream: TLInputStream): PQInnerData {
        val pq: ByteArray = inputStream.readWrappedBytes()
        val p: ByteArray = inputStream.readWrappedBytes()
        val q: ByteArray = inputStream.readWrappedBytes()
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val newNonce: TLInt256 = TLInt256.parse(inputStream)
        return PQInnerData(pq, p, q, nonce, serverNonce, newNonce)
      }
    }
  }

  /**
   * p_q_inner_data_dc#a9f55f95 pq:bytes p:bytes q:bytes nonce:int128 server_nonce:int128
   * new_nonce:int256 dc:int = P_Q_inner_data;
   */
  public data class PQInnerDataDc(
    public val pq: ByteArray,
    public val p: ByteArray,
    public val q: ByteArray,
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val newNonce: TLInt256,
    public val dc: Int,
  ) : TLPQInnerData {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeWrappedByteArray(pq)
      outputStream.writeWrappedByteArray(p)
      outputStream.writeWrappedByteArray(q)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      newNonce.serialize(outputStream)
      outputStream.writeInt(dc)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -1_443_537_003

      public fun parse(inputStream: TLInputStream): PQInnerDataDc {
        val pq: ByteArray = inputStream.readWrappedBytes()
        val p: ByteArray = inputStream.readWrappedBytes()
        val q: ByteArray = inputStream.readWrappedBytes()
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val newNonce: TLInt256 = TLInt256.parse(inputStream)
        val dc: Int = inputStream.readInt()
        return PQInnerDataDc(pq, p, q, nonce, serverNonce, newNonce, dc)
      }
    }
  }

  /**
   * p_q_inner_data_temp#3c6a84d4 pq:bytes p:bytes q:bytes nonce:int128 server_nonce:int128
   * new_nonce:int256 expires_in:int = P_Q_inner_data;
   */
  public data class PQInnerDataTemp(
    public val pq: ByteArray,
    public val p: ByteArray,
    public val q: ByteArray,
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val newNonce: TLInt256,
    public val expiresIn: Int,
  ) : TLPQInnerData {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeWrappedByteArray(pq)
      outputStream.writeWrappedByteArray(p)
      outputStream.writeWrappedByteArray(q)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      newNonce.serialize(outputStream)
      outputStream.writeInt(expiresIn)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 1_013_613_780

      public fun parse(inputStream: TLInputStream): PQInnerDataTemp {
        val pq: ByteArray = inputStream.readWrappedBytes()
        val p: ByteArray = inputStream.readWrappedBytes()
        val q: ByteArray = inputStream.readWrappedBytes()
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val newNonce: TLInt256 = TLInt256.parse(inputStream)
        val expiresIn: Int = inputStream.readInt()
        return PQInnerDataTemp(pq, p, q, nonce, serverNonce, newNonce, expiresIn)
      }
    }
  }

  /**
   * p_q_inner_data_temp_dc#56fddf88 pq:bytes p:bytes q:bytes nonce:int128 server_nonce:int128
   * new_nonce:int256 dc:int expires_in:int = P_Q_inner_data;
   */
  public data class PQInnerDataTempDc(
    public val pq: ByteArray,
    public val p: ByteArray,
    public val q: ByteArray,
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val newNonce: TLInt256,
    public val dc: Int,
    public val expiresIn: Int,
  ) : TLPQInnerData {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeWrappedByteArray(pq)
      outputStream.writeWrappedByteArray(p)
      outputStream.writeWrappedByteArray(q)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      newNonce.serialize(outputStream)
      outputStream.writeInt(dc)
      outputStream.writeInt(expiresIn)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 1_459_478_408

      public fun parse(inputStream: TLInputStream): PQInnerDataTempDc {
        val pq: ByteArray = inputStream.readWrappedBytes()
        val p: ByteArray = inputStream.readWrappedBytes()
        val q: ByteArray = inputStream.readWrappedBytes()
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val newNonce: TLInt256 = TLInt256.parse(inputStream)
        val dc: Int = inputStream.readInt()
        val expiresIn: Int = inputStream.readInt()
        return PQInnerDataTempDc(pq, p, q, nonce, serverNonce, newNonce, dc, expiresIn)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLPQInnerData {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        PQInnerData.CONSTRUCTOR_HASH -> PQInnerData.parse(inputStream)
        PQInnerDataDc.CONSTRUCTOR_HASH -> PQInnerDataDc.parse(inputStream)
        PQInnerDataTemp.CONSTRUCTOR_HASH -> PQInnerDataTemp.parse(inputStream)
        PQInnerDataTempDc.CONSTRUCTOR_HASH -> PQInnerDataTempDc.parse(inputStream)
        else -> throw TLObjectParseException(TLPQInnerData::class, constructorHash)
      }
    }
  }
}
