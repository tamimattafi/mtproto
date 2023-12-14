package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLFutureSalt : TLObject {
  /**
   * future_salt#0949d9dc valid_since:int valid_until:int salt:long = FutureSalt;
   */
  public data class FutureSalt(
    public val validSince: Int,
    public val validUntil: Int,
    public val salt: Long,
  ) : TLFutureSalt {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeInt(validSince)
      outputStream.writeInt(validUntil)
      outputStream.writeLong(salt)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 155_834_844

      public fun parse(inputStream: TLInputStream): FutureSalt {
        val validSince: Int = inputStream.readInt()
        val validUntil: Int = inputStream.readInt()
        val salt: Long = inputStream.readLong()
        return FutureSalt(validSince, validUntil, salt)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLFutureSalt {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        FutureSalt.CONSTRUCTOR_HASH -> FutureSalt.parse(inputStream)
        else -> throw TLObjectParseException(TLFutureSalt::class, constructorHash)
      }
    }
  }
}
