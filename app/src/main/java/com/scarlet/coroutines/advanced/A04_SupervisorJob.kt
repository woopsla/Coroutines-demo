package com.scarlet.coroutines.advanced

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

//
//         scope (Job()/SupervisorJob()) (😡) <--- cancel
//              |
//      +-------+-------+
//      |               |
//  child1(😡)       child2 (😡)
//
object Canceling_Scope_Cancels_its_Job_and_All_Children_Regardless_Of_Job_Types {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        coroutineContext.job.onCompletion("runBlocking")

        // What if change to Job()
        val scope = CoroutineScope(SupervisorJob())

        val child1 = scope.launch {
            log("child1 started")
            delay(1_000)
            log("child1 done")
        }.onCompletion("child 1")

        val child2 = scope.launch {
            log("child2 started")
            delay(1_000)
            log("child2 done")
        }.onCompletion("child 2")

        delay(500)

        scope.cancel()
        joinAll(child1, child2)

        log("is Parent job cancelled? = ${scope.coroutineContext.job.isCancelled}")
    }
}

//
//                     scope (Job()/SupervisorJob()) (✅)
//                          |
//                  +-------+-------+
//                  |               |
//  cancel --->  child1(😡)       child2 (✅)
//
object Canceling_A_Child_Cancels_Only_The_Target_Child_Including_All_Its_Descendants_If_Any {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        // What if change to Job()
        val scope = CoroutineScope(SupervisorJob())

        val child1 = scope.launch {
            log("child1 started")
            delay(1_000)
            log("child1 done")
        }.onCompletion("child 1")

        val child2 = scope.launch {
            log("child2 started")
            delay(1_000)
            log("child2 done")
        }.onCompletion("child 2")

        delay(500)

        child1.cancel()
        joinAll(child1, child2)

        log("is Parent cancelled? = ${scope.coroutineContext.job.isCancelled}")
    }
}

//
//         scope (Job()) (😡)                 scope (SupervisorJob()) (✅)
//              |                                  |
//      +-------+-------+                  +-------+-------+
//      |               |                  |               |
//  child1(🔥)       child2 (😡)      child1(🔥)       child2 (✅)
//
object SupervisorJob_Child_Failure_SimpleDemo {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        coroutineContext.job.onCompletion("runBlocking")

        // What if change to Job()
        val scope = CoroutineScope(Job())

        val child1 = scope.launch {
            log("child1 started")
            delay(500)
            throw RuntimeException("child 1 failed")
        }.onCompletion("child 1")

        val child2 = scope.launch {
            log("child2 started")
            delay(1_000)
            log("child2 done")
        }.onCompletion("child 2")

        joinAll(child1, child2)

        log("is scope cancelled? = ${scope.coroutineContext.job.isCancelled}")
    }
}
