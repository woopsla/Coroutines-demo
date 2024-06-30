package com.scarlet.coroutines.basics

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