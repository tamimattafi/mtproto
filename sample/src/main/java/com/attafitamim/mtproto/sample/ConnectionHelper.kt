package com.attafitamim.mtproto.sample

import com.attafitamim.mtproto.buffer.jvm.JavaByteBuffer
import com.attafitamim.mtproto.client.api.connection.IConnectionManager
import com.attafitamim.mtproto.client.connection.auth.DefaultAuthenticator
import com.attafitamim.mtproto.client.connection.auth.DefaultAuthenticatorStorage
import com.attafitamim.mtproto.client.connection.auth.IAuthenticator
import com.attafitamim.mtproto.client.connection.core.IConnection
import com.attafitamim.mtproto.client.connection.core.IConnectionProvider
import com.attafitamim.mtproto.client.connection.manager.ConnectionManager
import com.attafitamim.mtproto.client.connection.manager.ConnectionPassport
import com.attafitamim.mtproto.client.sockets.connection.SocketConnection
import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.Endpoint
import com.attafitamim.mtproto.client.sockets.infrastructure.endpoint.IEndpointProvider
import com.attafitamim.mtproto.client.sockets.infrastructure.socket.ISocketProvider
import com.attafitamim.mtproto.client.sockets.ktor.KtorWebSocketProvider
import com.attafitamim.mtproto.security.cipher.rsa.RsaKey
import com.attafitamim.mtproto.security.obfuscation.DefaultObfuscator
import com.attafitamim.mtproto.security.obfuscation.IObfuscator
import com.attafitamim.mtproto.security.utils.ISecureRandom
import com.russhwolf.settings.PreferencesSettings
import java.security.SecureRandom
import java.util.prefs.Preferences
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

object ConnectionHelper {

    private val retryInterval = 40.seconds.inWholeMilliseconds

    private val client by lazy {
        KtorModule.provideHttpClient()
    }

    val scope by lazy {
        CoroutineScope(Job() + Dispatchers.IO)
    }

    fun createConnectionManager(
        connectionProvider: IConnectionProvider,
        passport: ConnectionPassport
    ): IConnectionManager {
        val authenticator = createAuthenticator()
        return ConnectionManager(scope, connectionProvider, authenticator, passport)
    }

    fun createSocketProvider(
        endpointProvider: IEndpointProvider
    ) = KtorWebSocketProvider(
        client,
        scope,
        retryInterval,
        endpointProvider
    )

    fun createConnectionProvider(
        socketProvider: ISocketProvider
    ): IConnectionProvider {
        return object : IConnectionProvider {
            override fun provideConnection(): IConnection {
                val obfuscator = createObfuscator()
                val socket = socketProvider.provideSocket()
                return SocketConnection(
                    scope,
                    obfuscator,
                    socket
                )
            }
        }
    }


    private fun createSecureRandom(): ISecureRandom {
        val secureRandom = SecureRandom()

        return object : ISecureRandom {
            override fun getRandomBytes(size: Int): ByteArray {
                val bytes = ByteArray(size)
                secureRandom.nextBytes(bytes)

                return bytes
            }

            override fun getRandomLong(): Long =
                secureRandom.nextLong()
        }
    }

    private fun createAuthenticator(): IAuthenticator {
        val secureRandom = createSecureRandom()
        val preferences = Preferences.systemNodeForPackage(DefaultAuthenticator::class.java)
        val settings = PreferencesSettings(preferences)
        val storage = DefaultAuthenticatorStorage(settings)

        val serverKeys = listOf(rsaKey)
        return DefaultAuthenticator(secureRandom, storage, serverKeys)
    }

    private fun createObfuscator(): IObfuscator {
        val secureRandom = createSecureRandom()

        return DefaultObfuscator(
            secureRandom,
            JavaByteBuffer
        )
    }

    fun createdEndpointProvider(url: String) = IEndpointProvider {
        Endpoint.Url(url)
    }

    fun createdEndpointProvider(ip: String, port: Int) = IEndpointProvider {
        Endpoint.Address(ip, port)
    }
}
