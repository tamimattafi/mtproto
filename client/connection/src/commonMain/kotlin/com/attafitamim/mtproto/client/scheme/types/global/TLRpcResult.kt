package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLRpcResult : TLObject, TLProtocolMessage {
  /**
   * rpc_result#f35c6d01 req_msg_id:long result:byteArray = RpcResult;
   */
  public data class RpcResult(
    public val reqMsgId: Long,
    public val result: ByteArray,
  ) : TLRpcResult {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(reqMsgId)
      outputStream.writeByteArray(result)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -212_046_591

      public fun parse(inputStream: TLInputStream): RpcResult {
        val reqMsgId: Long = inputStream.readLong()
        val result: ByteArray = inputStream.readByteArray()
        return RpcResult(reqMsgId, result)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLRpcResult {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        RpcResult.CONSTRUCTOR_HASH -> RpcResult.parse(inputStream)
        else -> throw TLObjectParseException(TLRpcResult::class, constructorHash)
      }
    }
  }
}
