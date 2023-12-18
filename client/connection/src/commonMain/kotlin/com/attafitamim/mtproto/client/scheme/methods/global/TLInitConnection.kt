package com.attafitamim.mtproto.client.scheme.methods.global

import com.attafitamim.mtproto.client.scheme.types.global.TLInputClientProxy
import com.attafitamim.mtproto.core.serialization.helpers.SerializationHelper
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * initConnection#7d7f18cf {X:Type} flags:# api_id:int api_hash:flags.1?string device_model:string
 * system_version:string app_version:string system_lang_code:string lang_pack:string lang_code:string
 * proxy:flags.0?InputClientProxy query:X = X;
 */
public data class TLInitConnection<X : Any>(
  public val apiId: Int,
  public val apiHash: String?,
  public val deviceModel: String,
  public val systemVersion: String,
  public val appVersion: String,
  public val systemLangCode: String,
  public val langPack: String,
  public val langCode: String,
  public val proxy: TLInputClientProxy?,
  public val query: X,
  public val parseX: (inputStream: TLInputStream) -> X,
) : TLMethod<X> {
  override val constructorHash: Int = CONSTRUCTOR_HASH

  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeInt(constructorHash)
    var flags = 0
    flags = if (apiHash != null) (flags or 2) else (flags and 2.inv())
    flags = if (proxy != null) (flags or 1) else (flags and 1.inv())
    outputStream.writeInt(flags)
    outputStream.writeInt(apiId)
    if(apiHash != null) {
      outputStream.writeString(apiHash)
    }
    outputStream.writeString(deviceModel)
    outputStream.writeString(systemVersion)
    outputStream.writeString(appVersion)
    outputStream.writeString(systemLangCode)
    outputStream.writeString(langPack)
    outputStream.writeString(langCode)
    if(proxy != null) {
      proxy.serialize(outputStream)
    }
    SerializationHelper.serialize(outputStream, query)
  }

  override fun parse(inputStream: TLInputStream): X {
    val response: X = parseX.invoke(inputStream)
    return response
  }

  public companion object {
    public const val CONSTRUCTOR_HASH: Int = 2_105_481_423
  }
}
