package com.scarlet.coroutines.basics

import android.os.Build.VERSION_CODES.S
import java.util.concurrent.ThreadLocalRandom.current

object Continuation_Passing_Style {

    private fun add(a: Int, b: Int): Int = a + b
    private fun mul(a: Double, b: Double): Double = a * b

    // (1 + 2) * (3 + 4)

    private fun evaluate(): Double {
        // Label 0
        val step1 = add(1, 2)
        // Label 1
        val step2 = add(3, 4)
        // Label 2
        val step3 = mul(step1.toDouble(), step2.toDouble())
        // Label 3
        return step3
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(evaluate())

        println(fact(10))

        println(
            (0..10).map { fib(it.toLong()) }.joinToString(", ")
        )
    }

    // Exercise 1: Convert this to CPS style
    private fun fact(n: Long): Long =
        when (n) {
            0L -> 1L
            else -> n * fact(n - 1)
        }

    // Exercise 2: Convert this to CPS style
    private fun fib(n: Long): Long =
        when (n) {
            0L, 1L -> n
            else -> fib(n - 1) + fib(n - 2)
        }
}

object Continuation_Passing_Style_Demo {

    private fun add(a: Int, b: Int): Int = a + b
    private fun mul(a: Double, b: Double): Double = a * b

    private fun <R> addCPS(a: Int, b: Int, cont: (Int) -> R): R = cont(a + b)
    private fun <R> mulCPS(a: Double, b: Double, cont: (Double) -> R): R = cont(a * b)

    // (1 + 2) * (3 + 4)

    private fun <R> evaluateCPS(cont: (Double) -> R): R {
        // Label 0
        return addCPS(1, 2) { step1 ->
            // Label 1
            addCPS(3, 4) { step2 ->
                // Label 2
                mulCPS(step1.toDouble(), step2.toDouble()) { step3 ->
                    // Label 3
                    cont(step3)
                }
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        println(evaluateCPS { it })

        println(factCPS(10) { it })

        println(
            (0..10).map { fibCPS(it.toLong()) { i -> i } }.joinToString(", ")
        )
    }

    // Exercise 1: Convert this to CPS style
    private fun <R> factCPS(n: Long, cont: (Long) -> R): R =
        when (n) {
            0L -> cont(1L)
            else -> factCPS(n - 1) { prev ->
                cont(n * prev)
            }
        }

    // Exercise 2: Convert this to CPS style
    private fun <R> fibCPS(n: Long, cont: (Long) -> R): R =
        when (n) {
            0L, 1L -> cont(n)
            else -> fibCPS(n - 1) { current ->
                fibCPS(n - 2) { prev ->
                    cont(prev + current)
                }
            }
        }
}