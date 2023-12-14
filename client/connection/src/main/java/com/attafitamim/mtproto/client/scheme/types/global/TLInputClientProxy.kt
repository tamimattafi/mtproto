package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLInputClientProxy : TLObject {
  /**
   * inputClientProxy#75588b3f address:string port:int = InputClientProxy;
   */
  public data class InputClientProxy(
    public val address: String,
    public val port: Int,
  ) : TLInputClientProxy {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeString(address)
      outputStream.writeInt(port)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 1_968_737_087

      public fun parse(inputStream: TLInputStream): InputClientProxy {
        val address: String = inputStream.readString()
        val port: Int = inputStream.readInt()
        return InputClientProxy(address, port)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLInputClientProxy {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        InputClientProxy.CONSTRUCTOR_HASH -> InputClientProxy.parse(inputStream)
        else -> throw TLObjectParseException(TLInputClientProxy::class, constructorHash)
      }
    }
  }
}
