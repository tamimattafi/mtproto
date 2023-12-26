package com.attafitamim.mtproto.security.cipher.rsa

import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import com.attafitamim.mtproto.security.cipher.utils.toDERFormat
import com.attafitamim.mtproto.security.utils.CFMutableDictionary
import com.attafitamim.mtproto.security.utils.add
import com.attafitamim.mtproto.security.utils.releaseBridgeAs
import com.attafitamim.mtproto.security.utils.retainBridgeAs
import com.attafitamim.mtproto.security.utils.toByteArray
import com.attafitamim.mtproto.security.utils.toCFData
import com.attafitamim.mtproto.security.utils.useNSData
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFErrorRefVar
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Security.SecKeyAlgorithm
import platform.Security.SecKeyCreateEncryptedData
import platform.Security.SecKeyCreateWithData
import platform.Security.SecKeyRef
import platform.Security.kSecAttrKeyClass
import platform.Security.kSecAttrKeyClassPublic
import platform.Security.kSecAttrKeyType
import platform.Security.kSecAttrKeyTypeRSA
import platform.Security.kSecKeyAlgorithmRSAEncryptionRaw

@OptIn(ExperimentalStdlibApi::class, ExperimentalForeignApi::class)
actual open class RsaEcbCipher actual constructor(
    mode: CipherMode,
    rsaKey: RsaKey,
    padding: AlgorithmPadding
) : ICipher {

    protected val memScope = MemScope()
    protected val secKey: SecKeyRef
    protected val platformAlgorithm: SecKeyAlgorithm? = kSecKeyAlgorithmRSAEncryptionRaw

    init {
        val keyBytes = rsaKey.toDerPublicKey()
        val error = memScope.alloc<CFErrorRefVar>()

        val attributes = CFMutableDictionary(2.convert()) {
            add(kSecAttrKeyType, kSecAttrKeyTypeRSA)
            add(kSecAttrKeyClass, kSecAttrKeyClassPublic)
        }

        val generatedKey = SecKeyCreateWithData(
            keyBytes.toCFData(),
            attributes,
            error.ptr
        )

        secKey = requireNotNull(generatedKey)
    }

    override fun updateData(data: ByteArray): ByteArray {
        val error = memScope.alloc<CFErrorRefVar>()
        return data.useNSData { plaintext ->
            val ciphertext = SecKeyCreateEncryptedData(
                key = secKey,
                algorithm = platformAlgorithm,
                plaintext = plaintext.retainBridgeAs(),
                error = error.ptr
            )?.releaseBridgeAs<NSData>()

            if (ciphertext == null) {
                val nsError = error.value.releaseBridgeAs<NSError>()
                error("Failed to encrypt: ${nsError?.description}")
            }

            ciphertext.toByteArray()
        }
    }

    override fun finalize(data: ByteArray): ByteArray =
        updateData(data)

    protected fun RsaKey.toDerPublicKey(): ByteArray = when (this) {
        is RsaKey.Raw -> toDERFormat()
    }
}