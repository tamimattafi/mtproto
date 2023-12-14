package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLAccessPointRule : TLObject {
  /**
   * accessPointRule#4679b65f phone_prefix_rules:string dc_id:int ips:vector<IpPort> =
   * AccessPointRule;
   */
  public data class AccessPointRule(
    public val phonePrefixRules: String,
    public val dcId: Int,
    public val ips: TLVector<TLIpPort>,
  ) : TLAccessPointRule {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeString(phonePrefixRules)
      outputStream.writeInt(dcId)
      ips.serialize(outputStream)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 1_182_381_663

      public fun parse(inputStream: TLInputStream): AccessPointRule {
        val phonePrefixRules: String = inputStream.readString()
        val dcId: Int = inputStream.readInt()
        val ips: TLVector<TLIpPort> = TLVector.parse(inputStream, TLIpPort.Companion::parse)
        return AccessPointRule(phonePrefixRules, dcId, ips)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLAccessPointRule {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        AccessPointRule.CONSTRUCTOR_HASH -> AccessPointRule.parse(inputStream)
        else -> throw TLObjectParseException(TLAccessPointRule::class, constructorHash)
      }
    }
  }
}
