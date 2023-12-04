
package com.attafitamim.mtproto.client.sockets.serialization

import java.math.BigInteger
import java.util.*

internal class PQSolver {

    companion object {
        /**
         * Decomposes pq into prime factors such that p < q
         * Same implementation than https://github.com/enricostara/telegram-mt-node/blob/master/lib/security/pq-finder.js
         * TODO: check origin
         */
        @JvmStatic
        @SuppressWarnings("SuspiciousNameCombination")
        fun solve(input: BigInteger): SolvedPQ {
            val r = Random()
            var q: BigInteger = BigInteger.ZERO
            for (i in 0..2) {
                val w = BigInteger(((r.nextInt(128) and 15) + 17).toString())
                var x = BigInteger((r.nextInt(1000000000) + 1).toString())
                var y = x

                val lim = 1 shl (i + 18)
                for (j in 1..lim - 1) {
                    var a = x
                    var b = x
                    var c = w
                    while (b != BigInteger.ZERO) {
                        if ((b and BigInteger.ONE) != BigInteger.ZERO) {
                            c += a
                            if (c >= input) {
                                c -= input
                            }
                        }
                        a += a
                        if (a >= input) {
                            a -= input
                        }
                        b = b shr 1
                    }
                    x = c
                    val z = if (x < y) y - x else x - y // var z = y.gt(x) TODO why different here ?
                    q = GCD(z, input)
                    if (q != BigInteger.ONE)
                        break
                    if ((j and (j - 1)) == 0)
                        y = x
                }
                if (q > BigInteger.ONE)
                    break
            }

            val p = input / q
            return SolvedPQ(p.longValueExact(), q.longValueExact())
        }

        private fun GCD(x: BigInteger, y: BigInteger): BigInteger {
            var a = x
            var b = y
            while (a != BigInteger.ZERO && b != BigInteger.ZERO) {
                while ((b and BigInteger.ONE) == BigInteger.ZERO) {
                    b = b shr 1
                }
                while ((a and BigInteger.ONE) == BigInteger.ZERO) {
                    a = a shr 1
                }
                if (a > b) {
                    a -= b
                } else {
                    b -= a
                }
            }
            return if (b == BigInteger.ZERO) a else b
        }
    }
}

internal open class Pair<out F, out S>(val first: F, val second: S) {

    override fun equals(other: Any?) = other is Pair<*, *> && other.first == first && other.second == second

    override fun hashCode() = (first?.hashCode() ?: 0) xor (second?.hashCode() ?: 0)

    companion object {
        fun <A, B> create(a: A, b: B) = Pair(a, b)
    }
}

internal class SolvedPQ(p: Long, q: Long) : Pair<BigInteger, BigInteger>(BigInteger.valueOf(Math.min(p, q)), BigInteger.valueOf(Math.max(p, q))) {
    val p = first
    val q = second
}