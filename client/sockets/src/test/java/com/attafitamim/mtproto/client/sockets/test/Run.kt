package com.attafitamim.mtproto.client.sockets.test

import com.attafitamim.mtproto.client.sockets.test.helpers.ConnectionHelper
import com.attafitamim.mtproto.client.sockets.test.helpers.Playground
import com.attafitamim.scheme.mtproto.containers.global.TLInt128
import com.attafitamim.scheme.mtproto.methods.global.TLReqPq
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun main() {
    ConnectionHelper.scope.launch {
        val nonceBytes = byteArrayOf(-77, 54, -95, -75, 52, 102, -54, 107, 20, 10, -76, -123, 35, 77, -43, -17)
        val nonce = TLInt128(nonceBytes)
        val request = TLReqPq(nonce)
        println("Request: $request")

        val response = Playground.sendRequest(request)
        println("Response: $response")
    }

    while (ConnectionHelper.scope.isActive) {
        Thread.sleep(3000)
    }
}