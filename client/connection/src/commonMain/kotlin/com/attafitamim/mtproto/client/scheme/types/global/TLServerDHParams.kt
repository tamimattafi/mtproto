package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.client.scheme.containers.global.TLInt128
import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLServerDHParams : TLObject {
  /**
   * server_DH_params_fail#79cb045d nonce:int128 server_nonce:int128 new_nonce_hash:int128 =
   * Server_DH_Params;
   */
  public data class ServerDHParamsFail(
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val newNonceHash: TLInt128,
  ) : TLServerDHParams {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      newNonceHash.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 2_043_348_061

      public fun parse(inputStream: TLInputStream): ServerDHParamsFail {
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val newNonceHash: TLInt128 = TLInt128.parse(inputStream)
        return ServerDHParamsFail(nonce, serverNonce, newNonceHash)
      }
    }
  }

  /**
   * server_DH_params_ok#d0e8075c nonce:int128 server_nonce:int128 encrypted_answer:bytes =
   * Server_DH_Params;
   */
  public data class ServerDHParamsOk(
    public val nonce: TLInt128,
    public val serverNonce: TLInt128,
    public val encryptedAnswer: ByteArray,
  ) : TLServerDHParams {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      nonce.serialize(outputStream)
      serverNonce.serialize(outputStream)
      outputStream.writeWrappedByteArray(encryptedAnswer)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -790_100_132

      public fun parse(inputStream: TLInputStream): ServerDHParamsOk {
        val nonce: TLInt128 = TLInt128.parse(inputStream)
        val serverNonce: TLInt128 = TLInt128.parse(inputStream)
        val encryptedAnswer: ByteArray = inputStream.readWrappedBytes()
        return ServerDHParamsOk(nonce, serverNonce, encryptedAnswer)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLServerDHParams {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        ServerDHParamsFail.CONSTRUCTOR_HASH -> ServerDHParamsFail.parse(inputStream)
        ServerDHParamsOk.CONSTRUCTOR_HASH -> ServerDHParamsOk.parse(inputStream)
        else -> throw TLObjectParseException(TLServerDHParams::class, constructorHash)
      }
    }
  }
}
