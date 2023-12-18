package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLDestroyAuthKeyRes : TLObject {
  /**
   * destroy_auth_key_ok#f660e1d4 = DestroyAuthKeyRes;
   */
  public object DestroyAuthKeyOk : TLDestroyAuthKeyRes {
    public const val CONSTRUCTOR_HASH: Int = -161_422_892

    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
    }
  }

  /**
   * destroy_auth_key_none#0a9f2259 = DestroyAuthKeyRes;
   */
  public object DestroyAuthKeyNone : TLDestroyAuthKeyRes {
    public const val CONSTRUCTOR_HASH: Int = 178_201_177

    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
    }
  }

  /**
   * destroy_auth_key_fail#ea109b13 = DestroyAuthKeyRes;
   */
  public object DestroyAuthKeyFail : TLDestroyAuthKeyRes {
    public const val CONSTRUCTOR_HASH: Int = -368_010_477

    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLDestroyAuthKeyRes {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        DestroyAuthKeyOk.CONSTRUCTOR_HASH -> DestroyAuthKeyOk
        DestroyAuthKeyNone.CONSTRUCTOR_HASH -> DestroyAuthKeyNone
        DestroyAuthKeyFail.CONSTRUCTOR_HASH -> DestroyAuthKeyFail
        else -> throw TLObjectParseException(TLDestroyAuthKeyRes::class, constructorHash)
      }
    }
  }
}
