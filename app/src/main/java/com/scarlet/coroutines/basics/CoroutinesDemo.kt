package com.scarlet.coroutines.basics

import com.scarlet.util.log
import com.scarlet.util.spaces
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce

object Coroutines_Multitasking {

    private suspend fun task1() {
        for (i in 1..10) {
            log("${spaces(4)}Coroutine 1: $i")
            yield()
        }
    }

    private suspend fun task2() {
        for (i in 1..10) {
            log("${spaces(8)}Coroutine 2: $i")
            yield()
        }
    }

    private suspend fun task3() {
        for (i in 1..10) {
            log("${spaces(12)}Coroutine 3: $i")
            yield()
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val coroutine1 = launch { task1() }
        val coroutine2 = launch { task2() }
        val coroutine3 = launch { task3() }

        joinAll(coroutine1, coroutine2, coroutine3)
        log("Done!")
    }
}

object Generator {

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.generator(commandChannel: Channel<String>) = produce {
        var prev = 0
        var current = 1

        while (true) {
            println("Generator: Waiting for next request ...")
            commandChannel.receive() // wait for next
            println("Generator: Sending $prev")
            send(prev)
            prev = current.also {
                current += prev
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val commandChannel = Channel<String>()

        val receiveChannel = generator(commandChannel)

        for (i in 1..10) {
            commandChannel.send("next")
            receiveChannel.receive().also { gen ->
                println("Got result = $gen")
            }
            delay(1_000);
        }

        receiveChannel.cancel()
    }
}

object GeneratorUsingSequence {

    private fun fib(): Sequence<Int> = sequence {
        var previous = 0
        var current = 1
        while (true) {
            println("${spaces(4)}Generates $previous and waiting for next request")
            yield(previous)
            previous = current.also {
                current += previous
            }
        }
    }

    private fun prompt(): Boolean {
        print("next? ")
        return !readlnOrNull().equals("n")
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val iterator = fib().iterator()

        while (prompt()) {
            println("Got result = ${iterator.next()}")
        }
    }
}

