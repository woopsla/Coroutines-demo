package com.scarlet.coroutines.miscs

import java.math.BigInteger

object factorials {

    // Plain recursive factorial function
    fun factorial(n: Long): BigInteger =
        if (n <= 1) {
            BigInteger.ONE
        } else {
            n.toBigInteger() * factorial(n - 1)
        }

    @JvmStatic
    fun main(args: Array<String>) {
        println((0 until 10).map { factorial(it.toLong()) })
        println(factorial(10_000L))
    }
}

object factorials_TR {

    // Tail-recursive function
    tailrec
    fun factorial(n: Long, accumulator: BigInteger): BigInteger =
        if (n <= 1) {
            accumulator
        } else {
            factorial(n - 1, accumulator * n.toBigInteger())
        }

    @JvmStatic
    fun main(args: Array<String>) {
        println((0 until 10).map { factorial(it.toLong(), BigInteger.ONE) })
        println(factorial(10_000, BigInteger.ONE))
    }
}

// Lazy evaluation
object trampoline_demo1 {

    fun factorial(n: Long, accumulator: BigInteger): BigInteger =
        if (n <= 1) {
            accumulator
        } else {
            factorial(n - 1, accumulator * n.toBigInteger())
        }

    fun <T> run(f: () -> Any?): T = TODO()

    @JvmStatic
    fun main(args: Array<String>) {
        println((0 until 10).map { factorial(it.toLong(), BigInteger.ONE) })
        println(factorial(10_000, BigInteger.ONE))
    }
}

// Trampoline
object trampoline_demo2 {

    fun factorial(n: Long, accumulator: BigInteger): BigInteger =
        if (n <= 1) {
            accumulator
        } else {
            factorial(n - 1, accumulator * BigInteger.valueOf(n))
        }

    @JvmStatic
    fun main(args: Array<String>) {
        println((0 until 10).map { factorial(it.toLong(), BigInteger.ONE) })
        println(factorial(10_000, BigInteger.ONE))
    }
}