package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLMsgsAllInfo : TLObject {
  /**
   * msgs_all_info#8cc0d131 msg_ids:Vector<long> info:string = MsgsAllInfo;
   */
  public data class MsgsAllInfo(
    public val msgIds: TLVector<Long>,
    public val info: String,
  ) : TLMsgsAllInfo {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      msgIds.serialize(outputStream)
      outputStream.writeString(info)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -1_933_520_591

      public fun parse(inputStream: TLInputStream): MsgsAllInfo {
        val msgIds: TLVector<Long> = TLVector.parse(inputStream, { it.readLong() })
        val info: String = inputStream.readString()
        return MsgsAllInfo(msgIds, info)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLMsgsAllInfo {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        MsgsAllInfo.CONSTRUCTOR_HASH -> MsgsAllInfo.parse(inputStream)
        else -> throw TLObjectParseException(TLMsgsAllInfo::class, constructorHash)
      }
    }
  }
}
