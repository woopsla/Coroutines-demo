package com.scarlet.coroutines.miscs

import java.math.BigInteger

// Plain recursive factorial function
object trampoline_demo1 {
    
    fun factorial(n: Long): BigInteger =
        if (n <= 1) {
            BigInteger.ONE
        } else {
            BigInteger.valueOf(n) * factorial(n - 1)
        }

    @JvmStatic
    fun main(args: Array<String>) {
        println((0 until 10).map { factorial(it.toLong()) })
        println(factorial(10_000))
    }
}

// Tail-recursive function
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

// Lazy evaluation
object trampoline_demo3 {

    fun factorial(n: Long, accumulator: BigInteger): BigInteger =
        if (n <= 1) {
            accumulator
        } else {
            factorial(n - 1, accumulator * BigInteger.valueOf(n))
        }

    fun <T> run(f: () -> Any?): T = TODO()

    @JvmStatic
    fun main(args: Array<String>) {
        println((0 until 10).map { factorial(it.toLong(), BigInteger.ONE) })
        println(factorial(10_000, BigInteger.ONE))
    }
}

// Trampoline
object trampoline_demo4 {

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