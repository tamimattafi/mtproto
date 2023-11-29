package com.attafitamim.mtproto.client.sockets.test

import com.attafitamim.mtproto.client.sockets.test.helpers.Playground
import com.attafitamim.mtproto.client.sockets.test.helpers.RandomUtils.randomByteArray
import com.attafitamim.scheme.mtproto.containers.global.TLInt128
import com.attafitamim.scheme.mtproto.methods.global.TLReqPq
import io.ktor.util.moveToByteArray
import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.Random
import kotlinx.coroutines.test.runTest
import org.junit.Test


class TestKeyGenerate {


    @Test
    fun `test generating key`() = runTest {
        val nonceBytes = byteArrayOf(-77, 54, -95, -75, 52, 102, -54, 107, 20, 10, -76, -123, 35, 77, -43, -17)
        val nonce = TLInt128(nonceBytes)
        val request = TLReqPq(nonce)
        println("Request: $request")

        val response = Playground.sendRequest(request)
        println("Response: $response")
    }
}