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
     * `supervisorScope` has a `SupervisorJob()` and acts as a parent of root coroutines.
     *
     * `supervisorScope` does not rethrow an uncaught exception, but propagates it instead!!
     * (⚠️Beware: Applies only to propagated exception!!!! - Jungsun's note)
     *
     * Failure of a child coroutine does not propagate(?) to its parent. <== Seems like it does propagate.
     *
     * (⚠️Jungsun's note:
     * Actually, it does propagate to its parent. But, it does "not" cancel its parent. <== _Strange!!_)
     *
     * This feature requires an installed `CoroutineExceptionHandler` in its root coroutines,
     * otherwise the `supervisorScope` will fail anyway. That's because a scope always looks
     * for an installed exception handler. If it can't find any, it fails.
     *
     * ⚠️Important Note: Exceptions not propagated from child coroutines do not propagate,
     * (i.e., exceptions thrown `supervisorScope` body) they are rethrown instead unless it
     * is caught on-site.
     *
     * Recommendation by Jungsun: Always install a CEH when using `supervisorScope`.
     */

    @Test
    fun `supervisorScope propagates uncaught propagated exceptions - runBlocking`() =
        runBlocking<Unit> {
            // `supervisorScope` may call default CEH, which prints the stack trace.
            // In App, the default CEH crashes the App.
            // `runBlocking` regards this as the exception as handled and test pass.
            launch {
                supervisorScope {
                    onCompletion("supervisorScope")

                    launch {
                        delay(100)
                        throw RuntimeException("oops(❌)")
                    }.onCompletion("child1")
                }
                log("parent: Hey, I'm still alive!")
            }.onCompletion("parent")
        }

    @Test
    fun `supervisorScope propagates propagated uncaught exceptions - runTest`() = runTest {
        // `supervisorScope` calls installed CEH at TestScope, which prints the stack trace.
        // and `runTest` rethrows any first uncaught exception */
        launch {
            supervisorScope {
                onCompletion("supervisorScope")

                launch {
                    delay(100)
                    throw RuntimeException("oops(❌)")
                }.onCompletion("child1")
            }
            log("Hey, I'm still alive!")
        }.onCompletion("parent")
    }

    @Test
    fun `supervisorScope does not rethrow propagated exceptions so catch is useless`() =
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
    fun `supervisorScope - failed child doesn't affect its parent nor siblings`() = runTest {
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
//                coroutineContext.cancelChildren()
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