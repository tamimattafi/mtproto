package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLMsgDetailedInfo : TLObject {
  /**
   * msg_detailed_info#276d3ec6 msg_id:long answer_msg_id:long bytes:int status:int =
   * MsgDetailedInfo;
   */
  public data class MsgDetailedInfo(
    public val msgId: Long,
    public val answerMsgId: Long,
    public val bytes: Int,
    public val status: Int,
  ) : TLMsgDetailedInfo {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(msgId)
      outputStream.writeLong(answerMsgId)
      outputStream.writeInt(bytes)
      outputStream.writeInt(status)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 661_470_918

      public fun parse(inputStream: TLInputStream): MsgDetailedInfo {
        val msgId: Long = inputStream.readLong()
        val answerMsgId: Long = inputStream.readLong()
        val bytes: Int = inputStream.readInt()
        val status: Int = inputStream.readInt()
        return MsgDetailedInfo(msgId, answerMsgId, bytes, status)
      }
    }
  }

  /**
   * msg_new_detailed_info#809db6df answer_msg_id:long bytes:int status:int = MsgDetailedInfo;
   */
  public data class MsgNewDetailedInfo(
    public val answerMsgId: Long,
    public val bytes: Int,
    public val status: Int,
  ) : TLMsgDetailedInfo {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(answerMsgId)
      outputStream.writeInt(bytes)
      outputStream.writeInt(status)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -2_137_147_681

      public fun parse(inputStream: TLInputStream): MsgNewDetailedInfo {
        val answerMsgId: Long = inputStream.readLong()
        val bytes: Int = inputStream.readInt()
        val status: Int = inputStream.readInt()
        return MsgNewDetailedInfo(answerMsgId, bytes, status)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLMsgDetailedInfo {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        MsgDetailedInfo.CONSTRUCTOR_HASH -> MsgDetailedInfo.parse(inputStream)
        MsgNewDetailedInfo.CONSTRUCTOR_HASH -> MsgNewDetailedInfo.parse(inputStream)
        else -> throw TLObjectParseException(TLMsgDetailedInfo::class, constructorHash)
      }
    }
  }
}
