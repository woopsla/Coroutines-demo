package com.scarlet.coroutines.basics

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread
import kotlin.system.measureTimeMillis

object Threads {
    @JvmStatic
    fun main(args: Array<String>) {
        val time = measureTimeMillis {
            val jobs = List(100_000) {
                thread {
                    print(".")
                    Thread.sleep(1_000)
                }
            }
            jobs.forEach { it.join() }
        }
        println("\nElapses time = $time ms")
    }

}

object Coroutines {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val time = measureTimeMillis {
            val jobs = List(100_000) {
                launch {
                    print(".")
                    delay(1_000)
                }
            }
            jobs.forEach { it.join() }
        }
        println("\nElapses time = $time ms")
    }
}

object ThreadVsCoroutine {
    @DelicateCoroutinesApi
    @JvmStatic
    fun main(args: Array<String>) {
        threads()
//        coroutines()
    }

    // Main function waits for background work to finish
    private fun threads() {
        println("Main program starts: ${Thread.currentThread().name}")

        thread {
            println("Background work starts: ${Thread.currentThread().name}")
            Thread.sleep(1_000)
            println("Background work ends: ${Thread.currentThread().name}")
        }

        println("Main program ends: ${Thread.currentThread().name}")
    }

    @DelicateCoroutinesApi
    private fun coroutines() {
        println("Main program starts: ${Thread.currentThread().name}")

        GlobalScope.launch {
            println("Background work starts: ${Thread.currentThread().name}")
            Thread.sleep(1_000)
            println("Background work ends: ${Thread.currentThread().name}")
        }

        Thread.sleep(2_000)

        println("Main program ends: ${Thread.currentThread().name}")
    }

}

