package com.attafitamim.mtproto.client.scheme.types.help

import com.attafitamim.mtproto.client.scheme.types.global.TLAccessPointRule
import com.attafitamim.mtproto.client.scheme.types.global.TLVector
import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLHelpConfigSimple : TLObject {
  /**
   * help.configSimple#5a592a6c date:int expires:int rules:vector<AccessPointRule> =
   * help.ConfigSimple;
   */
  public data class ConfigSimple(
    public val date: Int,
    public val expires: Int,
    public val rules: TLVector<TLAccessPointRule>,
  ) : TLHelpConfigSimple {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeInt(date)
      outputStream.writeInt(expires)
      rules.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 1_515_793_004

      public fun parse(inputStream: TLInputStream): ConfigSimple {
        val date: Int = inputStream.readInt()
        val expires: Int = inputStream.readInt()
        val rules: TLVector<TLAccessPointRule> = TLVector.parse(inputStream,
            TLAccessPointRule.Companion::parse)
        return ConfigSimple(date, expires, rules)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLHelpConfigSimple {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        ConfigSimple.CONSTRUCTOR_HASH -> ConfigSimple.parse(inputStream)
        else -> throw TLObjectParseException(TLHelpConfigSimple::class, constructorHash)
      }
    }
  }
}
