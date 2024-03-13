
package com.attafitamim.mtproto.client.connection.auth

import kotlin.random.Random

object PQLongSolver {

    /**
     * Decomposes pq into prime factors such that p < q
     * Same implementation than https://github.com/enricostara/telegram-mt-node/blob/master/lib/security/pq-finder.js
     * TODO: check origin
     */
    fun solve(input: Long): SolvedLongPQ {
        var q = 0L
        for (i in 0..2) {
            val w = ((Random.nextLong(128) and 15) + 17)
            var x = (Random.nextLong(1000000000) + 1)
            var y = x

            val lim = 1 shl (i + 18)
            for (j in 1..< lim) {
                var a = x
                var b = x
                var c = w

                while (b != 0L) {
                    if ((b and 1L) != 0L) {
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

                if (q != 1L) {
                    break
                }

                if ((j and (j - 1)) == 0) {
                    y = x
                }
            }

            if (q > 1L) {
                break
            }
        }

        val p = input / q
        return SolvedLongPQ(
            p.coerceAtMost(q),
            p.coerceAtLeast(q)
        )
    }

    private fun gcd(x: Long, y: Long): Long {
        var a = x
        var b = y
        while (a != 0L && b != 0L) {
            while ((b and 1L) == 0L) {
                b = b shr 1
            }
            while ((a and 1L) == 0L) {
                a = a shr 1
            }
            if (a > b) {
                a -= b
            } else {
                b -= a
            }
        }
        return if (b == 0L) a else b
    }
}

data class SolvedLongPQ(
    val p: Long,
    val q: Long
)
