package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLDestroySessionRes : TLObject {
  /**
   * destroy_session_ok#e22045fc session_id:long = DestroySessionRes;
   */
  public data class DestroySessionOk(
    public val sessionId: Long,
  ) : TLDestroySessionRes {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(sessionId)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -501_201_412

      public fun parse(inputStream: TLInputStream): DestroySessionOk {
        val sessionId: Long = inputStream.readLong()
        return DestroySessionOk(sessionId)
      }
    }
  }

  /**
   * destroy_session_none#62d350c9 session_id:long = DestroySessionRes;
   */
  public data class DestroySessionNone(
    public val sessionId: Long,
  ) : TLDestroySessionRes {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(sessionId)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 1_658_015_945

      public fun parse(inputStream: TLInputStream): DestroySessionNone {
        val sessionId: Long = inputStream.readLong()
        return DestroySessionNone(sessionId)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLDestroySessionRes {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        DestroySessionOk.CONSTRUCTOR_HASH -> DestroySessionOk.parse(inputStream)
        DestroySessionNone.CONSTRUCTOR_HASH -> DestroySessionNone.parse(inputStream)
        else -> throw TLObjectParseException(TLDestroySessionRes::class, constructorHash)
      }
    }
  }
}
