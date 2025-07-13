package com.scarlet.coroutines.cancellation

import com.scarlet.util.completeStatus
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

/**
 * The **`Job`** interface has a method `cancel`, that allows its cancellation.
 * Calling it triggers the following effects:
 * - Such a coroutine ends the job _at the first suspension point_ (such as `delay()`).
 * - If a job has some children, they are canceled too.
 * - Once a job is canceled, it cannot be used as a parent for any new coroutines,
 *   it is first in _Cancelling_ and then in _Cancelled_ state.
 */

//
//          scope (❌)
//              |
//          parent (❌)
//              |
//          child1(❌)
//
object Cancel_Parent_Scope {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val scope = CoroutineScope(Job())

        var child: Job? = null
        val parent = scope.launch {
            child = launch {
                delay(1_000)
                log("child is done")
            }.onCompletion("child")
        }.onCompletion("parent")

        delay(100)

        // What should we do to wait for all the children to be completed in cancelled state?
        scope.cancel()

        log("parent cancelled = ${parent.isCancelled}")
        log("child cancelled = ${child?.isCancelled}")
        scope.completeStatus("scope")
    }
}

//
//          scope (✅)
//              |
//         Parent (❌)
//              |
//      +-------+-------+
//      |               |
//  child1(❌)       child2 (❌)
//
object Cancel_Parent_Coroutine {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val scope = CoroutineScope(Job())

        var child1: Job? = null
        var child2: Job? = null
        val parentJob = scope.launch {
            child1 =
                launch {
                    log("child1 started")
                    delay(1_000)
                }.onCompletion("child1")
            child2 =
                launch {
                    log("child2 started")
                    delay(1_000)
                }.onCompletion("child2")
        }.onCompletion("parentJob")

        delay(200)

        parentJob.cancelAndJoin()

        log("parent job cancelled = ${parentJob.isCancelled}")
        log("child1 job cancelled = ${child1?.isCancelled}")
        log("child2 job cancelled = ${child2?.isCancelled}")
        scope.completeStatus("scope")
    }

}

//
//          scope (✅)
//              |
//         Parent (✅)
//              |
//      +-------+-------+
//      |               |
//  child1(❌)       child2 (✅)
//
object Cancel_Child_Coroutine {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val scope = CoroutineScope(Job())

        var child1: Job? = null
        var child2: Job? = null
        val parentJob = scope.launch {
            child1 =
                launch {
                    log("child1 started")
                    delay(1_000)
                }.onCompletion("child1")
            child2 =
                launch {
                    log("child2 started")
                    delay(1_000)
                }.onCompletion("child2")
        }.onCompletion("parentJob")

        delay(200)

        child1?.cancel()
        parentJob.join()

        log("parent job cancelled = ${parentJob.isCancelled}")
        log("child1 job cancelled = ${child1?.isCancelled}")
        log("child2 job cancelled = ${child2?.isCancelled}")
        scope.completeStatus("scope")
    }
}

//
//          scope (✅)
//              |
//         Parent (✅)
//              |
//      +-------+-------+
//      |               |
//  child1(❌)       child2 (❌)
//
object Cancel_Children_Only_To_Reuse_Parent_Job {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val scope = CoroutineScope(Job())

        var child1: Job? = null
        var child2: Job? = null
        val parentJob = scope.launch {
            child1 =
                launch {
                    log("child1 started")
                    delay(1_000)
                }.onCompletion("child1")
            child2 =
                launch {
                    log("child2 started")
                    delay(1_000)
                }.onCompletion("child2")
        }.onCompletion("parentJob")

        delay(200)

        parentJob.cancelChildren()
        parentJob.join()

        log("parent job cancelled = ${parentJob.isCancelled}")
        log("child1 job cancelled = ${child1?.isCancelled}")
        log("child2 job cancelled = ${child2?.isCancelled}")
        scope.completeStatus("scope")
    }
}

//
//          scope (✅)
//              |
//         Parent (❌)
//              |
//      +-------+-------+
//      |               |
//  child1(❌)       child2 (❌)
//
object Cancel_Children_Only_To_Reuse_Scope {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val scope = CoroutineScope(Job())

        var child1: Job? = null
        var child2: Job? = null
        val parentJob = scope.launch {
            child1 =
                launch {
                    log("child1 started")
                    delay(1_000)
                }.onCompletion("child1")
            child2 =
                launch {
                    log("child2 started")
                    delay(1_000)
                }.onCompletion("child2")
        }.onCompletion("parentJob")

        delay(200)

        scope.coroutineContext.job.cancelChildren()
        parentJob.join()

        log("parent job cancelled = ${parentJob.isCancelled}")
        log("child1 job cancelled = ${child1?.isCancelled}")
        log("child2 job cancelled = ${child2?.isCancelled}")
        scope.completeStatus("scope")
    }
}

/**
 * Quiz
 */
object Cancel_Parent_Job_Quiz {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        val scope = CoroutineScope(Job())
        val job = Job()

        // Who's child's parent?
        val child = scope.launch(job) {
            delay(1_000)
        }.onCompletion("child")

        delay(100)

        // How to cancel the child via its parent?
        // job.cancelAndJoin() or scope.coroutineContext.job.cancelAndJoin()?
        scope.coroutineContext.job.cancelAndJoin()
//        job.cancelAndJoin()

        delay(1_000)
        log("child cancelled = ${child.isCancelled}")
        scope.completeStatus("scope")
    }
    //
    //   scope (❌)         Job (✅)
    //                         |
    //       +-----------------+
    //       |
    //   child (✅)
    //
}


