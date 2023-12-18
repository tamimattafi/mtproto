
package com.attafitamim.mtproto.client.connection.auth

import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlin.random.Random

internal class PQSolver {

    companion object {
        /**
         * Decomposes pq into prime factors such that p < q
         * Same implementation than https://github.com/enricostara/telegram-mt-node/blob/master/lib/security/pq-finder.js
         * TODO: check origin
         */
        fun solve(input: BigInteger): SolvedPQ {
            var q: BigInteger = BigInteger.ZERO
            for (i in 0..2) {
                val w = BigInteger(((Random.nextInt(128) and 15) + 17))
                var x = BigInteger((Random.nextInt(1000000000) + 1))
                var y = x

                val lim = 1 shl (i + 18)
                for (j in 1..< lim) {
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
                    q = gcd(z, input)
                    if (q != BigInteger.ONE)
                        break
                    if ((j and (j - 1)) == 0)
                        y = x
                }
                if (q > BigInteger.ONE)
                    break
            }

            val p = input / q
            return SolvedPQ(
                p.coerceAtMost(q),
                p.coerceAtLeast(q)
            )
        }

        private fun gcd(x: BigInteger, y: BigInteger): BigInteger {
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

data class SolvedPQ(
    val p: BigInteger,
    val q: BigInteger
)