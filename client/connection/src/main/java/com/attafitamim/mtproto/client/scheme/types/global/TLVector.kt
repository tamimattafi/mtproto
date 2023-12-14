package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.helpers.SerializationHelper
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLVector<T : Any> : TLObject {
  /**
   * vector#1cb5c415 {T:Type} elements:[T] = Vector<T>;
   */
  public data class Vector<T : Any>(
    public val elements: List<T>,
  ) : TLVector<T> {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeInt(elements.size)
      elements.forEach {
        SerializationHelper.serialize(outputStream, it)
      }
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 481_674_261

      public fun <T : Any> parse(inputStream: TLInputStream,
          parseT: (inputStream: TLInputStream) -> T): Vector<T> {
        val elementsSize: Int = inputStream.readInt()
        val elements = ArrayList<T>(elementsSize)
        while(elements.size < elementsSize) {
          val element: T = parseT.invoke(inputStream)
          elements.add(element)
        }
        return Vector<T>(elements)
      }
    }
  }

  public companion object {
    public fun <T : Any> parse(inputStream: TLInputStream,
        parseT: (inputStream: TLInputStream) -> T): TLVector<T> {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        Vector.CONSTRUCTOR_HASH -> Vector.parse(inputStream, parseT)
        else -> throw TLObjectParseException(TLVector::class, constructorHash)
      }
    }
  }
}
