package com.scarlet.coroutines.advanced

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

object Dependency_Between_Jobs {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        // coroutine starts when start() or join() called
        val job = launch(start = CoroutineStart.LAZY) {
            log("See when I am printed ...")
            delay(100)
            log("Pong")
        }

        delay(500)

        launch {
            log("Ping")
            job.join()
            log("Ping")
        }
    }
}

object Jobs_Form_Coroutines_Hierarchy {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val parentJob = launch(CoroutineName("Parent")) {
            launch {
                delay(1_000)
            }.onCompletion("Child1")

            // To check whether already finished child counted as children
            launch {
                delay(500)
            }.onCompletion("Child2")
        }.onCompletion("Parent")

        launch {
            delay(1_000)
        }.onCompletion("Sibling")

        delay(300)
        log("The parentJob has ${parentJob.children.count()} children")

        delay(500) // By this time, another child of the parentJob should have already been completed
        log("The parentJob has ${parentJob.children.count()} children")
    }
}

object In_Hierarchy_Parent_Waits_Until_All_Children_Finish {
    /**
     * Parental responsibilities:
     *
     * A parent coroutine always waits for completion of all its children.
     * A parent does not have to explicitly track all the children it launches,
     * and it does **not** have to use `Job.join` to wait for them at the end:
     */
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val parent = launch {
            repeat(3) { i ->
                launch { // try Dispatchers.Default
                    delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                    log("\t\tChild Coroutine $i is done")
                }
            }
            log("parent: I'm done, but will wait until all my children completes")
            // No need to join here
        }.onCompletion("parent: now, I am completed")

        parent.join() // wait for completion of the request, including all its children
        log("Done")
    }
}

/**
 * When the parent coroutine is cancelled, all its children are recursively cancelled,
 * too. However, this parent-child relation can be explicitly overridden in one
 * of two ways:
 *
 * 1. When a _different scope is explicitly specified_ when launching a coroutine
 *    (for example, `GlobalScope.launch`), then it does not inherit a coroutine
 *    context from the original parent scope.
 * 2. **When a different `Job` object is passed as the context for the new coroutine,
 *    then it overrides the Job of the parent scope.**
 *
 * In both cases, the launched coroutine is not tied to the scope it was launched
 * from and operates independently.
 */

object In_Hierarchy_Parent_Waits_Until_All_Children_Finish_Another_Demo {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val scope = CoroutineScope(Job())
        val parentJob = launch {
            log("I’m an adopting parent")
        }.onCompletion("Adopting Parent")

        scope.launch(parentJob) {
            log("\t\tI’m a child")
            delay(1_000)
        }.onCompletion("\t\tChild finished after 1000ms")

        delay(100)

        log("The scope (Real Parent) has ${scope.coroutineContext.job.children.count()} children at around 100ms")
        log("The parentJob (Adopting Parent) has ${parentJob.children.count()} children at around 100ms")
        log("is parentJob active at around 100ms? ${parentJob.isActive}")

        delay(500)
        log("is parentJob still active at around 600ms? ${parentJob.isActive}")

        parentJob.join()
        log("is parentJob still active after joined? ${parentJob.isActive}")
    }
}


