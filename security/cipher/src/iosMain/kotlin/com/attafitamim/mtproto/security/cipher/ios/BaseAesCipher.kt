package com.attafitamim.mtproto.security.cipher.ios

import com.attafitamim.mtproto.security.cipher.aes.AesKey
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmMode
import com.attafitamim.mtproto.security.cipher.algorithm.AlgorithmPadding
import com.attafitamim.mtproto.security.cipher.core.CipherMode
import com.attafitamim.mtproto.security.cipher.core.ICipher
import com.attafitamim.mtproto.security.cipher.exception.CryptographyException
import com.attafitamim.mtproto.security.cipher.utils.fixEmpty
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.ptr
import kotlinx.cinterop.refTo
import kotlinx.cinterop.value
import platform.CoreCrypto.CCAlgorithm
import platform.CoreCrypto.CCCryptorFinal
import platform.CoreCrypto.CCCryptorGetOutputLength
import platform.CoreCrypto.CCCryptorRefVar
import platform.CoreCrypto.CCCryptorRelease
import platform.CoreCrypto.CCCryptorStatus
import platform.CoreCrypto.CCCryptorUpdate
import platform.CoreCrypto.CCMode
import platform.CoreCrypto.CCModeOptions
import platform.CoreCrypto.CCOperation
import platform.CoreCrypto.CCPadding
import platform.CoreCrypto.ccNoPadding
import platform.CoreCrypto.ccPKCS7Padding
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCAlgorithmAES128
import platform.CoreCrypto.kCCAlignmentError
import platform.CoreCrypto.kCCBufferTooSmall
import platform.CoreCrypto.kCCDecodeError
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCMemoryFailure
import platform.CoreCrypto.kCCModeCBC
import platform.CoreCrypto.kCCModeCTR
import platform.CoreCrypto.kCCModeECB
import platform.CoreCrypto.kCCModeOptionCTR_BE
import platform.CoreCrypto.kCCParamError
import platform.CoreCrypto.kCCSuccess
import platform.CoreCrypto.kCCUnimplemented
import platform.posix.size_tVar

@OptIn(ExperimentalForeignApi::class)
abstract class BaseAesCipher(
    mode: CipherMode,
    algorithmMode: AlgorithmMode,
    padding: AlgorithmPadding,
    aesKey: AesKey
) : ICipher {

    protected val memScope = MemScope()

    protected val platformCipher = memScope.alloc<CCCryptorRefVar>()
    protected val platformCipherMode: CCOperation = mode.toPlatform()
    protected val platformAlgorithmMode: CCMode = algorithmMode.toPlatform()
    protected val platformAlgorithm: CCAlgorithm = aesKey.toPlatformAlgorithm()
    protected val platformPadding: CCPadding = padding.toPlatform()
    protected val cipherOptions: CCModeOptions = algorithmMode.toPlatformOptions()

    protected val keyRef = aesKey.key.refTo(0)
    protected val ivRef = aesKey.iv.refTo(0)

    protected val keySize = aesKey.key.size.toULong()
    protected val tweak = null
    protected val tweakLength = TWEAK_LENGTH.toULong()
    protected val numRounds = NUM_ROUNDS

    protected val dataOutMoved = memScope.alloc<size_tVar>()

    override fun updateData(data: ByteArray): ByteArray =
        data.update(final = false)

    override fun finalize(data: ByteArray): ByteArray {
        val output = data.update(final = true)
        val moved: Int = dataOutMoved.value.convert()

        val finalOutput = if (output.size != moved) {
            output.finalize(moved)
        } else {
            output
        }

        CCCryptorRelease(platformCipher.value)
        return finalOutput
    }

    private fun ByteArray.outputSize(
        final: Boolean
    ): Int = CCCryptorGetOutputLength(
        cryptorRef = platformCipher.value,
        inputLength = size.convert(),
        final = final
    ).convert()

    private fun ByteArray.update(final: Boolean): ByteArray {
        val outputSize = outputSize(final)
        val output = ByteArray(outputSize)

        val status = CCCryptorUpdate(
            cryptorRef = platformCipher.value,
            dataIn = fixEmpty().refTo(0),
            dataInLength = size.convert(),
            dataOut = output.fixEmpty().refTo(0),
            dataOutAvailable = output.size.convert(),
            dataOutMoved = dataOutMoved.ptr
        )

        handleStatus(status)
        return output
    }

    private fun ByteArray.finalize(moved: Int): ByteArray {
        val currentSize = size
        val available = currentSize - moved

        val status = CCCryptorFinal(
            cryptorRef = platformCipher.value,
            dataOut = refTo(moved),
            dataOutAvailable = available.convert(),
            dataOutMoved = dataOutMoved.ptr
        )

        handleStatus(status)

        val finalMoved = moved + dataOutMoved.value.toInt()
        return if (size == finalMoved) {
            this
        } else {
            copyOf(finalMoved)
        }
    }

    private fun CipherMode.toPlatform() = when (this) {
        CipherMode.ENCRYPT -> kCCEncrypt
        CipherMode.DECRYPT -> kCCDecrypt
    }

    private fun AlgorithmPadding.toPlatform() = when (this) {
        AlgorithmPadding.NONE -> ccNoPadding
        AlgorithmPadding.PKCS7 -> ccPKCS7Padding
    }

    private fun AesKey.toPlatformAlgorithm() = when (key.size) {
        AES_128_KEY_SIZE -> kCCAlgorithmAES128
        AES_256_KEY_SIZE -> kCCAlgorithmAES
        else -> error("Wrong aes key length ${key.size}")
    }

    private fun AlgorithmMode.toPlatform() = when (this) {
        AlgorithmMode.CTR -> kCCModeCTR
        AlgorithmMode.ECB -> kCCModeECB
        AlgorithmMode.CBC -> kCCModeCBC
        AlgorithmMode.GCM -> error("This class can't handle mode $this")
    }

    private fun AlgorithmMode.toPlatformOptions() = when (this) {
        AlgorithmMode.CTR -> kCCModeOptionCTR_BE
        AlgorithmMode.GCM,
        AlgorithmMode.ECB,
        AlgorithmMode.CBC -> 0u
    }

    protected fun handleStatus(result: CCCryptorStatus) {
        val error = when (result) {
            kCCSuccess        -> return
            kCCParamError     -> "Illegal parameter value."
            kCCBufferTooSmall -> "Insufficent buffer provided for specified operation."
            kCCMemoryFailure  -> "Memory allocation failure."
            kCCAlignmentError -> "Input size was not aligned properly."
            kCCDecodeError    -> "Input data did not decode or decrypt properly."
            kCCUnimplemented  -> "Function not implemented for the current algorithm."
            else              -> "CCCrypt failed with code $result"
        }

        throw CryptographyException(error)
    }

    private companion object {
        const val AES_128_KEY_SIZE = 16
        const val AES_256_KEY_SIZE = 32

        const val TWEAK_LENGTH = 0
        const val NUM_ROUNDS = 0
    }
}