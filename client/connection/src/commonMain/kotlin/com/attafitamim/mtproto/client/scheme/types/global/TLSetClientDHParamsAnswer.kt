package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLSetClientDHParamsAnswer : TLObject {
  /**
   * dh_gen_ok#3bcbf734 nonce:int128 server_nonce:int128 new_nonce_hash1:int128 =
   * Set_client_DH_params_answer;
   */
  public data class DhGenOk(
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val newNonceHash1: TLInt128,
  ) : TLSetClientDHParamsAnswer {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      newNonceHash1.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 1_003_222_836

      public fun parse(inputStream: TLInputStream): DhGenOk {
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val newNonceHash1: TLInt128 = TLInt128.parse(inputStream)
        return DhGenOk(nonce, serverNonce, newNonceHash1)
      }
    }
  }

  /**
   * dh_gen_retry#46dc1fb9 nonce:int128 server_nonce:int128 new_nonce_hash2:int128 =
   * Set_client_DH_params_answer;
   */
  public data class DhGenRetry(
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val newNonceHash2: TLInt128,
  ) : TLSetClientDHParamsAnswer {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      newNonceHash2.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 1_188_831_161

      public fun parse(inputStream: TLInputStream): DhGenRetry {
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val newNonceHash2: TLInt128 = TLInt128.parse(inputStream)
        return DhGenRetry(nonce, serverNonce, newNonceHash2)
      }
    }
  }

  /**
   * dh_gen_fail#a69dae02 nonce:int128 server_nonce:int128 new_nonce_hash3:int128 =
   * Set_client_DH_params_answer;
   */
  public data class DhGenFail(
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val newNonceHash3: TLInt128,
  ) : TLSetClientDHParamsAnswer {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      newNonceHash3.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -1_499_615_742

      public fun parse(inputStream: TLInputStream): DhGenFail {
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val newNonceHash3: TLInt128 = TLInt128.parse(inputStream)
        return DhGenFail(nonce, serverNonce, newNonceHash3)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLSetClientDHParamsAnswer {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        DhGenOk.CONSTRUCTOR_HASH -> DhGenOk.parse(inputStream)
        DhGenRetry.CONSTRUCTOR_HASH -> DhGenRetry.parse(inputStream)
        DhGenFail.CONSTRUCTOR_HASH -> DhGenFail.parse(inputStream)
        else -> throw TLObjectParseException(TLSetClientDHParamsAnswer::class, constructorHash)
      }
    }
  }
}
