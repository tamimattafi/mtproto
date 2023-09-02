package com.attafitamim.mtproto.client.android.tgnet;

import com.attafitamim.mtproto.core.serialization.streams.TLInputStream;
import com.attafitamim.mtproto.core.serialization.streams.TLOutputStream;

public abstract class AbstractSerializedData implements TLInputStream, TLOutputStream {

    public abstract int length();

    public abstract void skip(int count);

    public abstract int getPosition();

    public abstract int remaining();
}
