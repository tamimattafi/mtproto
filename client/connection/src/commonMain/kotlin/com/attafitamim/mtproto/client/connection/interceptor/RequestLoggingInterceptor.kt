package com.attafitamim.mtproto.client.connection.interceptor

class RequestLoggingInterceptor(
    private val logger: ((message: String) -> Unit)? = null
) : IRequestInterceptor {

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun <R : Any> intercept(chain: IRequestInterceptor.Chain<R>): R {
        val connectionType = chain.connectionSession.connectionType
        val sessionId = chain.connectionSession.session.id.toULong()
        val messageId = chain.messageId.toHexString()
        val method = chain.method
        logger?.invoke("session($sessionId) message($messageId) type($connectionType) sendRequest($method)")

        val result = kotlin.runCatching {
            chain.proceed(chain.method)
        }

        logger?.invoke("session($sessionId) message($messageId) type($connectionType) receivedResult($result)")
        return result.getOrThrow()
    }
}
