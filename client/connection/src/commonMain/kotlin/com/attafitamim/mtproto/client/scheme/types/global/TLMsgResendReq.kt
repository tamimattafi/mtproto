package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLMsgResendReq : TLObject, TLProtocolMessage {
  /**
   * msg_resend_req#7d861a08 msg_ids:Vector<long> = MsgResendReq;
   */
  public data class MsgResendReq(
    public val msgIds: TLVector<Long>,
  ) : TLMsgResendReq {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      msgIds.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 2_105_940_488

      public fun parse(inputStream: TLInputStream): MsgResendReq {
        val msgIds: TLVector<Long> = TLVector.parse(inputStream, { it.readLong() })
        return MsgResendReq(msgIds)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLMsgResendReq {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        MsgResendReq.CONSTRUCTOR_HASH -> MsgResendReq.parse(inputStream)
        else -> throw TLObjectParseException(TLMsgResendReq::class, constructorHash)
      }
    }
  }
}
