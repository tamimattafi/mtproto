package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLRpcError : TLObject {
  /**
   * rpc_error#2144ca19 error_code:int error_message:string = RpcError;
   */
  public data class RpcError(
    public val errorCode: Int,
    public val errorMessage: String,
  ) : TLRpcError {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeInt(errorCode)
      outputStream.writeString(errorMessage)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 558_156_313

      public fun parse(inputStream: TLInputStream): RpcError {
        val errorCode: Int = inputStream.readInt()
        val errorMessage: String = inputStream.readString()
        return RpcError(errorCode, errorMessage)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLRpcError {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        RpcError.CONSTRUCTOR_HASH -> RpcError.parse(inputStream)
        else -> throw TLObjectParseException(TLRpcError::class, constructorHash)
      }
    }
  }
}
