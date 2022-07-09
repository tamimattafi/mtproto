package com.attafitamim.mtproto.client.tgnet;

import org.jetbrains.annotations.Nullable;

public interface RequestDelegate<T> {
    void run(@Nullable T response, @Nullable RequestError error);
}
