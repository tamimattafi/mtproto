package com.attafitamim.mtproto.client.scheme.containers.global

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLContainer

/**
 * public_message msg_id:long seqno:int size:int data:bytes[size] = PublicMessage;
 */
public data class TLPublicMessage(
  public val msgId: Long,
  public val seqno: Int,
  public val size: Int,
  public val `data`: ByteArray,
) : TLContainer {
  override fun serialize(outputStream: TLOutputStream) {
    outputStream.writeLong(msgId)
    outputStream.writeInt(seqno)
    outputStream.writeInt(size)
    require(data.size == size)
    outputStream.writeByteArray(data)
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLPublicMessage {
      val msgId: Long = inputStream.readLong()
      val seqno: Int = inputStream.readInt()
      val size: Int = inputStream.readInt()
      val data: ByteArray = inputStream.readBytes(size)
      return TLPublicMessage(msgId, seqno, size, data)
    }
  }
}
