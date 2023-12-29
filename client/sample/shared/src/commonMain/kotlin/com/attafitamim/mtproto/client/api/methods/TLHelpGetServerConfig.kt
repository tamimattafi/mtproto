package com.attafitamim.mtproto.client.api.methods

import com.attafitamim.mtproto.client.api.types.TLDataJSON
import com.attafitamim.mtproto.core.serialization.streams.TLInputStream
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream
import com.attafitamim.mtproto.core.types.TLMethod

/**
 * help.getServerConfig#7217201a = DataJSON;
 */
public object TLHelpGetServerConfig : TLMethod<TLDataJSON> {
    public const val CONSTRUCTOR_HASH: Int = 1914118170

    public override val constructorHash: Int = CONSTRUCTOR_HASH

    public override fun serialize(outputStream: TLOutputStream): Unit {
        outputStream.writeInt(constructorHash)
    }

    public override fun parse(inputStream: TLInputStream): TLDataJSON {
        val response: TLDataJSON = TLDataJSON.parse(inputStream)
        return response
    }
}