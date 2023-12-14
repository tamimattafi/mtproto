package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLHttpWait : TLObject {
  /**
   * http_wait#9299359f max_delay:int wait_after:int max_wait:int = HttpWait;
   */
  public data class HttpWait(
    public val maxDelay: Int,
    public val waitAfter: Int,
    public val maxWait: Int,
  ) : TLHttpWait {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeInt(maxDelay)
      outputStream.writeInt(waitAfter)
      outputStream.writeInt(maxWait)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -1_835_453_025

      public fun parse(inputStream: TLInputStream): HttpWait {
        val maxDelay: Int = inputStream.readInt()
        val waitAfter: Int = inputStream.readInt()
        val maxWait: Int = inputStream.readInt()
        return HttpWait(maxDelay, waitAfter, maxWait)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLHttpWait {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        HttpWait.CONSTRUCTOR_HASH -> HttpWait.parse(inputStream)
        else -> throw TLObjectParseException(TLHttpWait::class, constructorHash)
      }
    }
  }
}
