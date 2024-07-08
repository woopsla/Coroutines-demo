package com.scarlet.coroutines.basics

import com.scarlet.util.log
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
//        threads()
        coroutines()
    }

    // Main function waits for background work to finish
    private fun threads() {
        log("Main program starts: ${Thread.currentThread().name}")

        thread {
            log("Background work starts: ${Thread.currentThread().name}")
            Thread.sleep(1_000)
            log("Background work ends: ${Thread.currentThread().name}")
        }

        println("Main program ends: ${Thread.currentThread().name}")
    }

    @DelicateCoroutinesApi
    private fun coroutines() {
        log("Main program starts: ${Thread.currentThread().name}")

        GlobalScope.launch {
            log("Background work starts: ${Thread.currentThread().name}")
            delay(1_000)
            log("Background work ends: ${Thread.currentThread().name}")
        }

        Thread.sleep(2_000)

        log("Main program ends: ${Thread.currentThread().name}")
    }

}

