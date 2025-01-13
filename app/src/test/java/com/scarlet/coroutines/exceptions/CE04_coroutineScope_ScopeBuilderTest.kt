package com.scarlet.coroutines.exceptions

import com.scarlet.util.completeStatus
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.lang.RuntimeException

class CoroutineScopeBuilderTest {

    /**
     * **coroutineScope** has a `Job()`.
     * A failing child causes the cancellation of its parent and siblings.
     * Does not propagate exception, just rethrows it!!
     */

    @Test
    fun `coroutineScope rethrows exception so it can be caught`() = runTest {
        try {
            // rethrows uncaught exception
            coroutineScope {
                onCompletion("coroutineScope")

                launch {
                    delay(100)
                    throw RuntimeException("oops(❌)")
                }.onCompletion("child")
            }

        } catch (ex: Exception) {
            log("Caught: $ex")
        }
    }

    @Test
    fun `coroutineScope - cancelling the scope cancels itself and all its children`() = runTest {
        try {
            coroutineScope {
                onCompletion("coroutineScope")

                launch { delay(500) }.onCompletion("child1")
                launch { delay(500) }.onCompletion("child2")

                delay(100)

                coroutineContext.cancel(CancellationException("Intentional cancellation(\uD83D\uDE4F\uD83C\uDFFC)"))
//                coroutineContext.cancelChildren()
            }
        } catch (ex: Exception) {
            log("Caught: $ex")
        }
    }

    @Test
    fun `coroutineScope - failing child causes cancellation of its parent and sibling`() = runTest {
        try {
            // rethrows uncaught exception
            coroutineScope {
                onCompletion("coroutineScope")

                launch {
                    delay(500)
                    throw RuntimeException("oops(❌)")
                }.onCompletion("child1")

                launch {
                    delay(1_000)
                }.onCompletion("child2")
            }
        } catch (ex: Exception) {
            log("Caught: $ex")
        }
    }

    //
    //                     scope (Job)(😡 or ✅) -- it depends!
    //                         |
    //                   parent Job(😡 or ✅) -- it depends!
    //                         |
    //                   coroutineScope (Job)(😡)
    //                         |
    //                 +-------+-------+
    //                 |               |
    //               child1(🔥)       child2(😡)
    //
    @Test
    fun `coroutineScope as a sub-scope of other coroutine`() = runTest {
        val scope = CoroutineScope(Job())

        val parentJob = scope.launch {
            try {
                coroutineScope {
                    onCompletion("coroutineScope")

                    launch {
                        delay(500)
                        throw RuntimeException("oops(❌)")
                    }.onCompletion("child1")

                    launch {
                        delay(1_000)
                    }.onCompletion("child2")
                }
            } catch (ex: Exception) {
                log("Caught: $ex")
            }
        }.onCompletion("parentJob")

        parentJob.join()
        scope.completeStatus("scope")
    }
}