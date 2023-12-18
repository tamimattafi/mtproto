package com.attafitamim.mtproto.client.scheme.methods.contest

import com.attafitamim.mtproto.client.scheme.types.global.TLBool
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * contest.saveDeveloperInfo#9a5f6e95 vk_id:int name:string phone_number:string age:int city:string
 * = Bool;
 */
public data class TLContestSaveDeveloperInfo(
  public val vkId: Int,
  public val name: String,
  public val phoneNumber: String,
  public val age: Int,
  public val city: String,
) : TLMethod<TLBool> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    outputStream.writeInt(vkId)
    outputStream.writeString(name)
    outputStream.writeString(phoneNumber)
    outputStream.writeInt(age)
    outputStream.writeString(city)
  }

  override fun parse(inputStream: TLInputStream): TLBool {
    val response: TLBool = TLBool.parse(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = -1_705_021_803
  }
}
