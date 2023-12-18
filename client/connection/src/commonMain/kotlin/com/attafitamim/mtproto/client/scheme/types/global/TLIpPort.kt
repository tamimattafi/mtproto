package com.attafitamim.mtproto.client.scheme.types.global

import com.attafitamim.mtproto.core.exceptions.TLObjectParseException
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLObject

public sealed interface TLIpPort : TLObject {
  /**
   * ipPort#d433ad73 ipv4:int port:int = IpPort;
   */
  public data class IpPort(
    public val ipv4: Int,
    public val port: Int,
  ) : TLIpPort {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeInt(ipv4)
      outputStream.writeInt(port)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = -734_810_765

      public fun parse(inputStream: TLInputStream): IpPort {
        val ipv4: Int = inputStream.readInt()
        val port: Int = inputStream.readInt()
        return IpPort(ipv4, port)
      }
    }
  }

  /**
   * ipPortSecret#37982646 ipv4:int port:int secret:bytes = IpPort;
   */
  public data class IpPortSecret(
    public val ipv4: Int,
    public val port: Int,
    public val secret: ByteArray,
  ) : TLIpPort {
    override val constructorHash: Int = CONSTRUCTOR_HASH

    override fun serialize(outputStream: TLOutputStream) {
      outputStream.writeInt(constructorHash)
      outputStream.writeInt(ipv4)
      outputStream.writeInt(port)
      outputStream.writeWrappedByteArray(secret)
    }

    public companion object {
      public const val CONSTRUCTOR_HASH: Int = 932_718_150

      public fun parse(inputStream: TLInputStream): IpPortSecret {
        val ipv4: Int = inputStream.readInt()
        val port: Int = inputStream.readInt()
        val secret: ByteArray = inputStream.readWrappedBytes()
        return IpPortSecret(ipv4, port, secret)
      }
    }
  }

  public companion object {
    public fun parse(inputStream: TLInputStream): TLIpPort {
      val constructorHash: Int = inputStream.readInt()
      return when(constructorHash) {
        IpPort.CONSTRUCTOR_HASH -> IpPort.parse(inputStream)
        IpPortSecret.CONSTRUCTOR_HASH -> IpPortSecret.parse(inputStream)
        else -> throw TLObjectParseException(TLIpPort::class, constructorHash)
      }
    }
  }
}
