package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLFutureSalts : TLObject, TLProtocolMessage {
  /**
   * future_salts#ae500895 req_msg_id:long now:int salts:vector<future_salt> = FutureSalts;
   */
  public data class FutureSalts(
    public val reqMsgId: Long,
    public val now: Int,
    public val salts: TLVector<TLFutureSalt>,
  ) : TLFutureSalts {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(reqMsgId)
      outputStream.writeInt(now)
      salts.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -1_370_486_635

      public fun parse(inputStream: TLInputStream): FutureSalts {
        val reqMsgId: Long = inputStream.readLong()
        val now: Int = inputStream.readInt()
        val salts: TLVector<TLFutureSalt> = TLVector.parse(inputStream,
            TLFutureSalt.Companion::parse)
        return FutureSalts(reqMsgId, now, salts)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLFutureSalts {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        FutureSalts.CONSTRUCTOR_HASH -> FutureSalts.parse(inputStream)
        else -> throw TLObjectParseException(TLFutureSalts::class, constructorHash)
      }
    }
  }
}
