package com.attafitamim.mtproto.client.scheme.containers.global

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLContainer

/**
 * secure_message key_id:bytes[8] msg_key:bytes[16] encrypted_data:byteArray = SecureMessage;
 */
public data class TLSecureMessage(
  public val keyId: ByteArray,
  public val msgKey: ByteArray,
  public val encryptedData: ByteArray,
) : TLContainer {
  override fun serialize(outputStream: TLOutputStream) {
    require(keyId.size == 8)
    outputStream.writeByteArray(keyId)
    require(msgKey.size == 16)
    outputStream.writeByteArray(msgKey)
    outputStream.writeByteArray(encryptedData)
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLSecureMessage {
      val keyId: ByteArray = inputStream.readBytes(8)
      val msgKey: ByteArray = inputStream.readBytes(16)
      val encryptedData: ByteArray = inputStream.readByteArray()
      return TLSecureMessage(keyId, msgKey, encryptedData)
    }
  }
}
