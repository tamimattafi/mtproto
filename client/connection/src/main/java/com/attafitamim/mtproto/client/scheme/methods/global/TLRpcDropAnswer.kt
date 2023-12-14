package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.types.global.TLRpcDropAnswer
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * rpc_drop_answer#58e4a740 req_msg_id:long = RpcDropAnswer;
 */
public data class TLRpcDropAnswer(
  public val reqMsgId: Long,
) : TLMethod<TLRpcDropAnswer> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    outputStream.writeLong(reqMsgId)
  }

  override fun parse(inputStream: TLInputStream):
      com.attafitamim.mtproto.client.scheme.types.global.TLRpcDropAnswer {
    val response: com.attafitamim.mtproto.client.scheme.types.global.TLRpcDropAnswer =
        TLRpcDropAnswer.parse(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = 1_491_380_032
  }
}
