package com.scarlet.coroutines.basics

import android.util.Log.e
import com.scarlet.util.coroutineInfo
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

//
//             Top-level Coroutine
//                     |
//               Level 1 Coroutine
//                     |
//               Level 2 Coroutine
//                     |
//        +------------+-----------+
//        |                        |
//  Level 3 Coroutine    Another Level 3 Coroutine
//
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

//
//         Parent (❌)
//              |
//      +-------+-------+
//      |               |
//  child1(❌)       child2 (❌)
//
object Canceling_parent_coroutine_cancels_the_parent_and_its_children {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val parent = launch {
            val child1 = launch {
                log("\t\tchild1 started")
                delay(1_000)
                log("\t\tchild1 done")
            }.onCompletion("child1")

            val child2 = launch {
                log("\t\tchild2 started")
                delay(1_000)
                log("\t\tchild2 done")
            }.onCompletion("child2")

            log("\tparent is waiting")
            joinAll(child1, child2)
            log("\tparent done")
        }.onCompletion("Parent")

//        parent.join()
        delay(500)
        parent.cancel() // parent.cancelAndJoin()

        log("Done")
    }
}

//
//         Parent (✅)
//              |
//      +-------+-------+
//      |               |
//  child1(❌)       child2 (✅)
//
object Canceling_a_child_cancels_only_the_child {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var child1: Job? = null

        val parent = launch {
            child1 = launch {
                log("\t\tchild1 started")
                delay(1_000)
                log("\t\tchild1 done")
            }.onCompletion("child1")

            val child2 = launch {
                log("\t\tchild2 started")
                delay(1_000)
                log("\t\tchild2 done")
            }.onCompletion("child2")

            log("\tparent is waiting")
            joinAll(child1, child2)
            log("\tparent done")
        }.onCompletion("parent")

        delay(500)

        child1?.cancel()
        parent.join()

        log("Done")
    }
}

//
//         Parent (🔥)
//              |
//      +-------+-------+
//      |               |
//  child1(❌)       child2 (❌)
//
object Failed_parent_causes_cancellation_of_all_children {

    @JvmStatic
    // rethrow uncaught propagated exceptions
    fun main(args: Array<String>) = runBlocking {
        coroutineContext.job.onCompletion("runBlocking")

        val parent = launch {
            launch {
                log("\t\tchild1 started")
                delay(1_000)
                log("\t\tchild1 done")
            }.onCompletion("child1")

            launch {
                log("\t\tchild2 started")
                delay(1_000)
                log("\t\tchild2 done")
            }.onCompletion("child2")

            delay(500)
            throw RuntimeException("\tparent failed")
        }.onCompletion("Parent")

        parent.join()

        log("Done.")
    }
}

//
//         Runblocking (❌)
//              |
//          Parent (❌)
//              |
//      +-------+-------+
//      |               |
//  child1(🔥)       child2 (❌)
//
object Failed_child_causes_cancellation_of_its_parent_and_siblings {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        coroutineContext.job.onCompletion("runBlocking")

        val parent = launch {
            val child1 = launch {
                log("\t\tchild1 started")
                delay(500)
                throw RuntimeException("child 1 failed")
            }.onCompletion("child1")

            val child2 = launch {
                log("\t\tchild2 started")
                delay(1_000)
                log("\t\tchild2 done")
            }.onCompletion("child2")

            log("\tparent is waiting")
            joinAll(child1, child2)
            log("\tparent done")
        }.onCompletion("Parent")

        parent.join()

        log("Done.")
    }
}

