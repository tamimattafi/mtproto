package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLNewSession : TLObject {
  /**
   * new_session_created#9ec20908 first_msg_id:long unique_id:long server_salt:long = NewSession;
   */
  public data class NewSessionCreated(
    public val firstMsgId: Long,
    public val uniqueId: Long,
    public val serverSalt: Long,
  ) : TLNewSession {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeLong(firstMsgId)
      outputStream.writeLong(uniqueId)
      outputStream.writeLong(serverSalt)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -1_631_450_872

      public fun parse(inputStream: TLInputStream): NewSessionCreated {
        val firstMsgId: Long = inputStream.readLong()
        val uniqueId: Long = inputStream.readLong()
        val serverSalt: Long = inputStream.readLong()
        return NewSessionCreated(firstMsgId, uniqueId, serverSalt)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLNewSession {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        NewSessionCreated.CONSTRUCTOR_HASH -> NewSessionCreated.parse(inputStream)
        else -> throw TLObjectParseException(TLNewSession::class, constructorHash)
      }
    }
  }
}
