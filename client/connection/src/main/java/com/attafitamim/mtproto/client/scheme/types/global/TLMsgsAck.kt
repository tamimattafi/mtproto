package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLMsgsAck : TLObject, TLProtocolMessage {
  /**
   * msgs_ack#62d6b459 msg_ids:Vector<long> = MsgsAck;
   */
  public data class MsgsAck(
    public val msgIds: TLVector<Long>,
  ) : TLMsgsAck {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      msgIds.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 1_658_238_041

      public fun parse(inputStream: TLInputStream): MsgsAck {
        val msgIds: TLVector<Long> = TLVector.parse(inputStream, { it.readLong() })
        return MsgsAck(msgIds)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLMsgsAck {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        MsgsAck.CONSTRUCTOR_HASH -> MsgsAck.parse(inputStream)
        else -> throw TLObjectParseException(TLMsgsAck::class, constructorHash)
      }
    }
  }
}
