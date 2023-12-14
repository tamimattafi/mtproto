package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLMsgsStateInfo : TLObject {
  /**
   * msgs_state_info#04deb57d req_msg_id:long info:string = MsgsStateInfo;
   */
  public data class MsgsStateInfo(
    public val reqMsgId: Long,
    public val info: String,
  ) : TLMsgsStateInfo {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(reqMsgId)
      outputStream.writeString(info)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 81_704_317

      public fun parse(inputStream: TLInputStream): MsgsStateInfo {
        val reqMsgId: Long = inputStream.readLong()
        val info: String = inputStream.readString()
        return MsgsStateInfo(reqMsgId, info)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLMsgsStateInfo {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        MsgsStateInfo.CONSTRUCTOR_HASH -> MsgsStateInfo.parse(inputStream)
        else -> throw TLObjectParseException(TLMsgsStateInfo::class, constructorHash)
      }
    }
  }
}
