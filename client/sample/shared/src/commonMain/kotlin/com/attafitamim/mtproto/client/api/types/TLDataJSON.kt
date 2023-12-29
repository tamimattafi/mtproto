package com.attafitamim.mtproto.client.api.types

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLDataJSON : TLObject {
  /**
   * dataJSON#7d748d04 data:string = DataJSON;
   */
  public data class DataJSON(
    public val `data`: String,
  ) : TLDataJSON {
    public override val constructorHash: Int = CONSTRUCTOR_HASH

    public override fun serialize(outputStream: TLOutputStream): Unit {
      outputStream.writeInt(constructorHash)
      outputStream.writeString(data)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 2104790276

      public fun parse(inputStream: TLInputStream): DataJSON {
        val data: String = inputStream.readString()
        return DataJSON(data)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLDataJSON {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        DataJSON.CONSTRUCTOR_HASH -> DataJSON.parse(inputStream)
        else -> throw TLObjectParseException(TLDataJSON::class, constructorHash)
      }
    }
  }
}
