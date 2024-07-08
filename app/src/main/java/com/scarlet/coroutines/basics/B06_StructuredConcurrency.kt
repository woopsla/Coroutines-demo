package com.scarlet.coroutines.basics

import com.scarlet.util.log
import kotlinx.coroutines.*
import java.lang.RuntimeException

object Nested_Coroutines {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        log("Top-Level Coroutine")

        launch {
            log("\tLevel 1 Coroutine")

            launch {
                log("\t\tLevel 2 Coroutine")

                launch { log("\t\t\tLevel 3 Coroutine") }
                launch { log("\t\t\tLevel 3 Another Coroutine") }
            }
        }
    }
}

/**
 * Structured Concurrency Preview
 */

object Canceling_parent_coroutine_cancels_the_parent_and_its_children {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val parent = launch {
            val child1 = launch {
                log("\t\tchild1 started")
                delay(1_000)
                log("\t\tchild1 done")
            }

            val child2 = launch {
                log("\t\tchild2 started")
                delay(1_000)
                log("\t\tchild2 done")
            }

            log("\tparent is waiting")
            joinAll(child1, child2)
            log("\tparent done")
        }

        parent.join()
//        delay(500)
//        parent.cancel() // parent.cancelAndJoin()

        log("Done")
    }
}

object Canceling_a_child_cancels_only_the_child {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var child1: Job? = null

        val parent = launch {
            child1 = launch {
                log("\t\tchild1 started")
                delay(1_000)
                log("\t\tchild1 done")
            }

            val child2 = launch {
                log("\t\tchild2 started")
                delay(1_000)
                log("\t\tchild2 done")
            }

            log("\tparent is waiting")
            joinAll(child1!!, child2)
            log("\tparent done")
        }

        delay(500)

        child1?.cancel()
        parent.join()

        log("Done")
    }
}

object Failed_parent_causes_cancellation_of_all_children {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val parent = launch {
            launch {
                log("\t\tchild1 started")
                delay(1_000)
                log("\t\tchild1 done")
            }

            launch {
                log("\t\tchild2 started")
                delay(1_000)
                log("\t\tchild2 done")
            }

            delay(500)
            throw RuntimeException("\tparent failed")
        }

        parent.join()

        log("Done.")
    }
}

object Failed_child_causes_cancellation_of_its_parent_and_siblings {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val parent = launch {
            val child1 = launch {
                log("\t\tchild1 started")
                delay(500)
                throw RuntimeException("child 1 failed")
            }

            val child2 = launch {
                log("\t\tchild2 started")
                delay(1_000)
                log("\t\tchild2 done")
            }

            log("\tparent is waiting")
            joinAll(child1, child2)
            log("\tparent done")
        }

        parent.join()

        log("Done.")
    }
}

