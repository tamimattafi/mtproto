package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLBool : TLObject {
  /**
   * boolFalse#bc799737 = Bool;
   */
  public object BoolFalse : TLBool {
    public const val CONSTRUCTOR_HASH: Int = -1_132_882_121

    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
    }
  }

  /**
   * boolTrue#997275b5 = Bool;
   */
  public object BoolTrue : TLBool {
    public const val CONSTRUCTOR_HASH: Int = -1_720_552_011

    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLBool {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        BoolFalse.CONSTRUCTOR_HASH -> BoolFalse
        BoolTrue.CONSTRUCTOR_HASH -> BoolTrue
        else -> throw TLObjectParseException(TLBool::class, constructorHash)
      }
    }
  }
}
