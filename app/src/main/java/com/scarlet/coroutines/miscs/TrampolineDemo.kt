package com.scarlet.coroutines.miscs

import io.reactivex.schedulers.Schedulers.trampoline
import java.math.BigInteger
import java.math.BigInteger.ONE

object factorials {

    // Plain recursive factorial function
    fun factorial(n: Long): BigInteger =
        if (n <= 1) {
            ONE
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

    // Tail-recursive function (TCO)
    tailrec
    fun factorial(n: Long, accumulator: BigInteger): BigInteger =
        if (n <= 1) {
            accumulator
        } else {
            factorial(n - 1, accumulator * n.toBigInteger())
        }

    @JvmStatic
    fun main(args: Array<String>) {
        println((0 until 10).map { factorial(it.toLong(), ONE) })
        println(factorial(10_000, ONE))
    }
}

// Lazy evaluation
object trampoline_demo1 {

    // Thunk: () -> Any?
    fun factorial(n: Long, accumulator: BigInteger): () -> Any? =
        if (n <= 1) {
            { accumulator }
        } else {
            { factorial(n - 1, accumulator * n.toBigInteger()) } // () -> Any?
        }

    @Suppress("UNCHECKED_CAST")
    fun <T> run(f: () -> Any?): T {
        var current = f()

        while (current is () -> Any?) {
            current = current()
        }
        return current as T
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println((0 until 10).map { run<BigInteger>(factorial(it.toLong(), ONE)) })
        println(run<BigInteger>(factorial(10_000, ONE)))
    }
}

// Trampoline
object trampoline_demo2 {

    sealed interface Trampoline<out T> {
        data class Done<T>(val result: T) : Trampoline<T>
        data class More<T>(val resume: () -> Trampoline<T>) : Trampoline<T>
    }

    fun <T> run(thunk: Trampoline<T>): T {
        var current: Trampoline<T> = thunk
        while (true) {
            when (current) {
                is Trampoline.Done -> return current.result
                is Trampoline.More -> current = current.resume()
            }
        }
    }
    
    fun factorial(n: Long, accumulator: BigInteger): Trampoline<BigInteger> =
        if (n <= 1) {
            Trampoline.Done(accumulator)
        } else {
            Trampoline.More{ factorial(n - 1, accumulator * BigInteger.valueOf(n)) }
        }

    @JvmStatic
    fun main(args: Array<String>) {
        println((0 until 10).map { run(factorial(it.toLong(), ONE)) })
        println(run(factorial(10_000, ONE)))
    }
}