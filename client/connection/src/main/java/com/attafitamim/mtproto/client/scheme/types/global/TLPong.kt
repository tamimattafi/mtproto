package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLPong : TLObject {
  /**
   * pong#347773c5 msg_id:long ping_id:long = Pong;
   */
  public data class Pong(
    public val msgId: Long,
    public val pingId: Long,
  ) : TLPong {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(msgId)
      outputStream.writeLong(pingId)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 880_243_653

      public fun parse(inputStream: TLInputStream): Pong {
        val msgId: Long = inputStream.readLong()
        val pingId: Long = inputStream.readLong()
        return Pong(msgId, pingId)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLPong {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        Pong.CONSTRUCTOR_HASH -> Pong.parse(inputStream)
        else -> throw TLObjectParseException(TLPong::class, constructorHash)
      }
    }
  }
}
