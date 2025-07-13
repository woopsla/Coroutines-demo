package com.scarlet.coroutines.exceptions

import com.scarlet.util.completeStatus
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

/**
 * **SupervisorJob** - failing child does not affect the parent and its sibling.
 *
 *          SupervisorJob (✅)
 *               |
 *       +-------+-------+
 *       |               |
 *    child1(🔥)       child2 (✅)
 */

class LaunchSupervisorJobTest {

    @Test
    fun `SupervisorJob in failing child's parent context takes effect`() = runTest {
        onCompletion("runTest")

        val scope = CoroutineScope(SupervisorJob()) // Compare with Job()

        val child1 = scope.launch {
            delay(100)
            throw RuntimeException("oops(❌)")
        }.onCompletion("child1")

        val child2 = scope.launch {
            delay(200)
        }.onCompletion("child2")

        joinAll(child1, child2)
        scope.completeStatus("scope")
    }


    /**
     * Quiz: Who's child1's parent? SupervisorJob or Job?
     */
    //
    //       runTest (Job)            SupervisorJob (✅) (
    //           |                          |
    //           +--> 😭                 parent (😡)
    //              parent                  |
    //                              +-------+-------+
    //                              |               |
    //                          child1(🔥)       child2(😡)
    //
    @Test
    fun `lecture note example - who's child1's parent`() = runTest {
        onCompletion("runTest")

        val parent = launch(Job()) {
            launch {
                delay(100)
                throw IOException("failure(❌)")
            }.onCompletion("child1")

            launch {
                delay(200)
            }.onCompletion("child2")
        }.onCompletion("parent")

        parent.join()
    }

    //
    //       scope (Job)            sharedJob (SupervisorJob)
    //           |                           |
    //           +-> 😭,😭          +-------+-------+
    //            child 1 & 2       |               |
    //                          child1(🔥)       child2(✅)
    //
    @Test
    fun `SupervisorJob in parent context controls the lifetime of children`() = runTest {
        val scope = CoroutineScope(Job())
        val sharedJob = SupervisorJob()

        val child1 = scope.launch(sharedJob) {
            delay(100)
            throw RuntimeException("oops(❌)")
        }.onCompletion("child1")

        val child2 = scope.launch(sharedJob) {
            delay(200)
        }.onCompletion("child2")

        joinAll(child1, child2)
        sharedJob.completeStatus("sharedJob")
        scope.completeStatus("scope")
    }

    //
    //       scope (Job✅) ---+            sharedJob (SupervisorJob)(✅)
    //           |            |                     |
    //    +------+-------+    +->😭,😭     +-------+-------+
    //    |              |                 |               |
    //  child3(✅)    child4(✅)       child1(🔥)      child2(✅)
    //
    @Test
    fun `SupervisorJob in parent context controls only the lifetime of its own children`() =
        runTest {
            val scope = CoroutineScope(Job())
            val sharedJob = SupervisorJob()

            val child1 = scope.launch(sharedJob) {
                delay(100)
                throw RuntimeException("oops")
            }.onCompletion("child1")

            val child2 = scope.launch(sharedJob) {
                delay(200)
            }.onCompletion("child2")

            val child3 = scope.launch {
                delay(200)
            }.onCompletion("child3")

            val child4 = scope.launch {
                delay(200)
            }.onCompletion("child4")

            joinAll(child1, child2, child3, child4)
            sharedJob.completeStatus("sharedJob")
            scope.completeStatus()
        }

    //
    //   scope (Job✅)    SupervisorJob
    //      |                  |
    //      +-> 😭         parent(😡)
    //                         |
    //                 +-------+-------+
    //                 |               |
    //               child1(🔥)       child2(😡)
    //
    @Test
    fun `SupervisorJob does not work when it is not part of the failing child's direct parent context`() =
        runTest {
            val scope = CoroutineScope(Job()).onCompletion("scope")
            try {
                val parent = scope.launch(SupervisorJob()) {
                    launch {
                        delay(100)
                        throw RuntimeException("oops")
                    }.onCompletion("child1")

                    launch {
                        delay(1_000)
                    }.onCompletion("child2")
                }.onCompletion("parent")

                parent.join()

            } catch (ex: Exception) {
                log("Exception caught: $ex") // Useless
            }
        }
}
