package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLRpcDropAnswer : TLObject {
  /**
   * rpc_answer_unknown#5e2ad36e = RpcDropAnswer;
   */
  public object RpcAnswerUnknown : TLRpcDropAnswer {
    public const val CONSTRUCTOR_HASH: Int = 1_579_864_942

    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
    }
  }

  /**
   * rpc_answer_dropped_running#cd78e586 = RpcDropAnswer;
   */
  public object RpcAnswerDroppedRunning : TLRpcDropAnswer {
    public const val CONSTRUCTOR_HASH: Int = -847_714_938

    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
    }
  }

  /**
   * rpc_answer_dropped#a43ad8b7 msg_id:long seq_no:int bytes:int = RpcDropAnswer;
   */
  public data class RpcAnswerDropped(
    public val msgId: Long,
    public val seqNo: Int,
    public val bytes: Int,
  ) : TLRpcDropAnswer {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(msgId)
      outputStream.writeInt(seqNo)
      outputStream.writeInt(bytes)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -1_539_647_305

      public fun parse(inputStream: TLInputStream): RpcAnswerDropped {
        val msgId: Long = inputStream.readLong()
        val seqNo: Int = inputStream.readInt()
        val bytes: Int = inputStream.readInt()
        return RpcAnswerDropped(msgId, seqNo, bytes)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLRpcDropAnswer {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        RpcAnswerUnknown.CONSTRUCTOR_HASH -> RpcAnswerUnknown
        RpcAnswerDroppedRunning.CONSTRUCTOR_HASH -> RpcAnswerDroppedRunning
        RpcAnswerDropped.CONSTRUCTOR_HASH -> RpcAnswerDropped.parse(inputStream)
        else -> throw TLObjectParseException(TLRpcDropAnswer::class, constructorHash)
      }
    }
  }
}
