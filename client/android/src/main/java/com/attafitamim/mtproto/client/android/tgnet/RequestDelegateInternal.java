package com.attafitamim.mtproto.client.android.tgnet;

public interface RequestDelegateInternal {
    void run(long response, int errorCode, String errorText, int networkType);
}
