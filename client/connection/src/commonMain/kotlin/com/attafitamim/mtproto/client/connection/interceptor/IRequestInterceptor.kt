package com.attafitamim.mtproto.client.connection.interceptor

import com.attafitamim.mtproto.client.connection.manager.ConnectionSession
import com.attafitamim.mtproto.core.types.TLMethod

interface IRequestInterceptor {

    suspend fun <R : Any> intercept(chain: Chain<R>): R

    abstract class Chain<R : Any>(
        val position: Int,
        val messageId: Long,
        val method: TLMethod<R>,
        val connectionSession: ConnectionSession
    ) {

        abstract suspend fun proceed(newMethod: TLMethod<R>): R
    }
}
