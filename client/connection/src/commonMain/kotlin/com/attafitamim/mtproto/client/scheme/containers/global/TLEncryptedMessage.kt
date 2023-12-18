package com.attafitamim.mtproto.client.scheme.containers.global

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLContainer

/**
 * encrypted_message server_salt:long session_id:long data:byteArray = EncryptedMessage;
 */
public data class TLEncryptedMessage(
  public val serverSalt: Long,
  public val sessionId: Long,
  public val `data`: ByteArray,
) : TLContainer {
  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeLong(serverSalt)
    outputStream.writeLong(sessionId)
    outputStream.writeByteArray(data)
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLEncryptedMessage {
      val serverSalt: Long = inputStream.readLong()
      val sessionId: Long = inputStream.readLong()
      val data: ByteArray = inputStream.readByteArray()
      return TLEncryptedMessage(serverSalt, sessionId, data)
    }
  }
}
