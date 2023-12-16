package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLBadMsgNotification : TLObject, TLProtocolMessage {
  /**
   * bad_msg_notification#a7eff811 bad_msg_id:long bad_msg_seqno:int error_code:int =
   * BadMsgNotification;
   */
  public data class BadMsgNotification(
    public val badMsgId: Long,
    public val badMsgSeqno: Int,
    public val errorCode: Int,
  ) : TLBadMsgNotification {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(badMsgId)
      outputStream.writeInt(badMsgSeqno)
      outputStream.writeInt(errorCode)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -1_477_445_615

      public fun parse(inputStream: TLInputStream): BadMsgNotification {
        val badMsgId: Long = inputStream.readLong()
        val badMsgSeqno: Int = inputStream.readInt()
        val errorCode: Int = inputStream.readInt()
        return BadMsgNotification(badMsgId, badMsgSeqno, errorCode)
      }
    }
  }

  /**
   * bad_server_salt#edab447b bad_msg_id:long bad_msg_seqno:int error_code:int new_server_salt:long
   * = BadMsgNotification;
   */
  public data class BadServerSalt(
    public val badMsgId: Long,
    public val badMsgSeqno: Int,
    public val errorCode: Int,
    public val newServerSalt: Long,
  ) : TLBadMsgNotification {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(badMsgId)
      outputStream.writeInt(badMsgSeqno)
      outputStream.writeInt(errorCode)
      outputStream.writeLong(newServerSalt)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -307_542_917

      public fun parse(inputStream: TLInputStream): BadServerSalt {
        val badMsgId: Long = inputStream.readLong()
        val badMsgSeqno: Int = inputStream.readInt()
        val errorCode: Int = inputStream.readInt()
        val newServerSalt: Long = inputStream.readLong()
        return BadServerSalt(badMsgId, badMsgSeqno, errorCode, newServerSalt)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLBadMsgNotification {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        BadMsgNotification.CONSTRUCTOR_HASH -> BadMsgNotification.parse(inputStream)
        BadServerSalt.CONSTRUCTOR_HASH -> BadServerSalt.parse(inputStream)
        else -> throw TLObjectParseException(TLBadMsgNotification::class, constructorHash)
      }
    }
  }
}
