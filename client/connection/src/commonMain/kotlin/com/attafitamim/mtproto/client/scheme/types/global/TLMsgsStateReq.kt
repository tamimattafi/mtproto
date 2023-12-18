package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLMsgsStateReq : TLObject {
  /**
   * msgs_state_req#da69fb52 msg_ids:Vector<long> = MsgsStateReq;
   */
  public data class MsgsStateReq(
    public val msgIds: TLVector<Long>,
  ) : TLMsgsStateReq {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      msgIds.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -630_588_590

      public fun parse(inputStream: TLInputStream): MsgsStateReq {
        val msgIds: TLVector<Long> = TLVector.parse(inputStream, { it.readLong() })
        return MsgsStateReq(msgIds)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLMsgsStateReq {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        MsgsStateReq.CONSTRUCTOR_HASH -> MsgsStateReq.parse(inputStream)
        else -> throw TLObjectParseException(TLMsgsStateReq::class, constructorHash)
      }
    }
  }
}
