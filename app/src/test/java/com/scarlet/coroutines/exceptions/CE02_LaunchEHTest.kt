package com.scarlet.coroutines.exceptions

import com.scarlet.util.completeStatus
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.lang.RuntimeException

/**
 * **Top-level coroutines**: coroutines without parent coroutine.
 *
 * **Root coroutines**:
 * - Coroutines that are a direct child of a `CoroutineScope` instance or `supervisorScope`
 * - All child coroutines (coroutines created in the context of another Job) delegate
 *   handling of their exceptions to their parent coroutine, which also delegates to the
 *   parent, and so on until the root.
 *
 *  Root scope do not propagate exceptions. Do default behavior of either printing
 *  to console or crash application.
 */

class LaunchEHTest {

    private fun failingFunction() {
        throw RuntimeException("oops")
    }

    @Test(expected = RuntimeException::class)
    fun `exception thrown`() {
        failingFunction()
    }

    // Both `runBlocking` and `runTest` rethrow uncaught exceptions.
    @Test(expected = RuntimeException::class)
    fun `exception with runBlocking or runTest`() = runTest {
        failingFunction()
    }

    // Both `runBlocking` and `runTest` rethrow only the first propagated uncaught exceptions.
    @Test
    fun `rethrows only the first uncaught exception`() = runTest {
        onCompletion("runTest")

        launch {
            delay(10)
            throw RuntimeException("yellow")
        }
        launch {
            delay(50)
            throw IOException("mellow")
        }
    }

    @Test
    fun `can handle rethrown exception on site using try-catch`() = runTest {
        launch {
            try {
                failingFunction()
            } catch (ex: Exception) {
                log("Caught $ex")
            }
        }
    }

    @Test(expected = RuntimeException::class)
    fun `propagated exception from nested coroutines cannot be handled on site using try-catch`() =
        runTest {
            try {
                launch {
                    failingFunction()
                }
            } catch (ex: Exception) {
                log("Caught $ex")  // useless
            }
        }

    /**
     * `runBlocking` vs. `runTest`
     *
     * `runBlocking` do not rethrow uncaught propagated exception reached at scope.
     *
     * But, `runTest` do rethrow it.
     */
    @Test // Try runBlocking ...
    fun `Failure of child cancels the parent and its siblings`() = runTest {
        onCompletion("runTest")

        // Same behavior even if `SupervisorJob` is used.
        val scope = CoroutineScope(Job()).onCompletion("scope")

        val parentJob = scope.launch {
            launch {
                delay(100)
                throw RuntimeException("oops")
            }.onCompletion("child1")

            launch {
                delay(1_000)
            }.onCompletion("child2")
        }.onCompletion("parentJob")

        parentJob.join()

        scope.completeStatus("scope")
    }

    @Test // Try runBlocking ...
    fun `Failure of child cancels the parent and its siblings2`() = runTest {
        onCompletion("runTest")

        val parentJob = launch {
            launch {
                delay(100)
                throw RuntimeException("oops")
            }.onCompletion("child1")

            launch {
                delay(1_000)
            }.onCompletion("child2")
        }.onCompletion("parentJob")

        parentJob.join()
    }
}

