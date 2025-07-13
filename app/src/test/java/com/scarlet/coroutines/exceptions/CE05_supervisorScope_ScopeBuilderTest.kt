package com.scarlet.coroutines.exceptions

import com.scarlet.util.completeStatus
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.lang.RuntimeException

class SupervisorScopeBuilderTest {

    /**
     * `supervisorScope` has a `SupervisorJob()` and acts as a parent of _root coroutines_.
     *
     * Unlike `coroutineScope`, `supervisorScope` does not rethrow propagated uncaught exceptions.
     * - Failure of a child coroutine does not propagate to `supervisorScope`'s parent.
     *
     * In App, `supervisorScope` needs an installed `CoroutineExceptionHandler` in
     * its root coroutines or scope, otherwise the `supervisorScope` will fail anyway causing crash.
     * That's because a scope always looks for an installed exception handler. If it
     * can't find any, it fails (by calling current thread's `Thread.uncaughtExceptionHandler`).
     * - **Recommendation** by Jungsun: Always install a CEH when using `supervisorScope`.
     *
     * ⚠️**Important Note**: Exceptions not propagated from child coroutines (i.e.,
     * exceptions thrown `supervisorScope` body) are rethrown unless caught on-site.
     */

    // `supervisorScope` may call default CEH, which prints the stack trace.
    // In App, the default CEH crashes the App.
    // `runBlocking` regards this as the exception as handled and test pass.
    @Test
    fun `supervisorScope does not propagate exceptions - runBlocking`() =
        runBlocking<Unit> {
            launch {
                supervisorScope {
                    onCompletion("supervisorScope")

                    launch {
                        delay(100)
                        throw RuntimeException("oops(❌)")
                    }.onCompletion("child1")

                    launch {
                        delay(200)
                    }.onCompletion("child2")
                }
                log("parent: Hey, I'm still alive!")
            }.onCompletion("parent")
        }

    // `runTest` rethrows any first uncaught exception at the end of the test.
    @Test
    fun `supervisorScope does not propagate uncaught exceptions - runTest`() = runTest {
        launch {
            supervisorScope {
                onCompletion("supervisorScope")

                launch {
                    delay(100)
                    throw RuntimeException("oops(❌)")
                }.onCompletion("child1")

                launch {
                    delay(200)
                }.onCompletion("child2")
            }
            log("Hey, I'm still alive!")
        }.onCompletion("parent")
    }

    @Test
    fun `supervisorScope does not rethrow propagated exceptions so catching is useless`() =
        runTest {
            onCompletion("runTest")

            try {
                supervisorScope {
                    onCompletion("supervisorScope")

                    val child = launch {
                        delay(500)
                        throw RuntimeException("oops(❌)")
                    }.onCompletion("child")

                    child.join()
                }
            } catch (ex: Exception) {
                log("Caught: $ex") // useless
            }
        }

    @Test
    fun `supervisorScope rethrows its own exceptions including cancellation`() = runTest {
        try {
            // Rethrows its own uncaught exception!!!
            supervisorScope {
                onCompletion("supervisorScope")

                launch {
                    delay(500)
                }.onCompletion("child")

                delay(100)
                throw RuntimeException("Oops(❌)")
            }

        } catch (ex: Exception) {
            log("Caught: $ex")
        }
    }

    @Test
    fun `supervisorScope - cancelling the scope cancels itself and all its children`() = runTest {
        try {
            // Rethrows its own uncaught exception
            supervisorScope {
                onCompletion("supervisorScope")

                launch { delay(500) }.onCompletion("child1")
                launch { delay(500) }.onCompletion("child2")

                delay(100)

                coroutineContext.cancel(CancellationException("Intentional cancellation(\uD83D\uDE4F\uD83C\uDFFC)"))
            }
        } catch (ex: Exception) {
            log("Caught: $ex")
            if (ex is CancellationException) {
                throw ex
            }
        }
    }

    /**
     * Quiz: Who's my parent again?
     */
    @Test
    fun `supervisorScope - quiz1`() = runTest {
        val scope = CoroutineScope(Job())

        supervisorScope {
            onCompletion("supervisorScope")

            scope.launch {
                launch { delay(100); throw RuntimeException("oops(❌)") }.onCompletion("child1")
                launch { delay(200) }.onCompletion("child2")
            }.onCompletion("parent job")
                .join()  // why do we need this?
        }

        scope.completeStatus("scope")
    }

    @Test
    fun `supervisorScope - quiz2`() = runTest {
        val scope = CoroutineScope(Job())

        scope.launch {
            onCompletion("supervisorScope")

            supervisorScope {
                launch { delay(100); throw RuntimeException("oops(❌)") }.onCompletion("child1")
                launch { delay(200) }.onCompletion("child2")
            }
        }.onCompletion("parent job").join()

        scope.completeStatus("scope")
    }
}

