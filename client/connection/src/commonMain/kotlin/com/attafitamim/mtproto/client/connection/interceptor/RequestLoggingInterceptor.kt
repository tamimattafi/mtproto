package com.attafitamim.mtproto.client.connection.interceptor

class RequestLoggingInterceptor(
    val log: (message: String) -> Unit = ::println
) : IRequestInterceptor {

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun <R : Any> intercept(chain: IRequestInterceptor.Chain<R>): R {
        val sessionId = chain.connectionSession.session.id.toULong()
        val messageId = chain.messageId.toHexString()
        val method = chain.method
        log("session($sessionId) message($messageId) sendRequest($method)")

        val result = kotlin.runCatching {
            chain.proceed(chain.method)
        }

        log("session($sessionId) message($messageId) receivedResult($result)")
        return result.getOrThrow()
    }
}
