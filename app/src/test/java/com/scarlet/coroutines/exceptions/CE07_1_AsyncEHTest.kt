package com.scarlet.coroutines.exceptions

import com.scarlet.util.completeStatus
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import com.scarlet.util.testDispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * **Root Coroutines**
 * - Direct child of `scope`
 * - Direct child of `supervisorScope`
 * ```
 *                scope           supervisorScope
 *                  |                   |
 *            root coroutine       root coroutine
 *```
 * ⚠️**Jungsun Kim's note:**
 * Documentation says the _exposed exception_ will be silently dropped unless
 * `.await()` is called on the deferred value.
 *
 * However, actually it propagates and cancels all siblings and the scope (if `Job()` is used)
 * regardless of whether `.await()` is called or not **without failing the test**.
 * Therefore, structured concurrency works anyhow, but
 * - we have a chance to handle exceptions using `try-catch`, and
 * - `runTest` will pass the test if either `.await()` not called, or called within `try-catch`.
 */
class AsyncEH01Test {
    private val ehandler = CoroutineExceptionHandler { context, exception ->
        log("Caught in handler: $exception, context = $context")
    }

    @Test
    fun `try-catch inside async works normally`() = runTest {
        onCompletion("runTest")

        val deferred = async {
            try {
                throw RuntimeException("Oops(❌)")
            } catch (ex: Exception) {
                log("Caught: $ex") // caught
            }
            42
        }.onCompletion("deferred")

        log("result = ${deferred.await()}")
    }

    ///////////////////////////////////////////////////////////////////////////////////
    // For Root Coroutines
    // - Direct child of `scope`
    ///////////////////////////////////////////////////////////////////////////////////

    /**
     * Even though an exception is caught, it affects the parent scope.
     * That it, t structured concurrency still works.
     * Test passes because the exception is considered as handled and there are no uncaught exceptions.
     */
    @Test
    fun `direct child of scope - catch on await`() = runTest {
        onCompletion("runTest")

        // `Job` or `SupervisorJob` does matter.
        val scope = CoroutineScope(Job() + ehandler).onCompletion("scope") // ehandler of no use

        val deferred: Deferred<Int> = scope.async(testDispatcher) { // root coroutine
            delay(1_000)
            throw RuntimeException("Oops(❌)") // exposed exception
        }.onCompletion("deferred")

        try {
            deferred.await()
        } catch (ex: Exception) {
            log("Caught: $ex") // Caught and handled
        }
    }

    @Test
    fun `direct child of scope - await not called`() = runTest {
        onCompletion("runTest")

        val scope = CoroutineScope(Job()).onCompletion("scope")

        // a root coroutine
        val deferred: Deferred<Int> = scope.async(testDispatcher) {
            delay(1_000)
            throw RuntimeException("Oops(❌)") // exposed exception
        }.onCompletion("deferred")

        // No `.await()` called, check whether exception will be silently dropped.
        delay(1_500)
    }

    @Test
    fun `direct child of scope - await called without try-catch`() = runTest {
        onCompletion("runTest")

        val scope = CoroutineScope(Job()).onCompletion("scope")

        // a root coroutine
        val deferred: Deferred<Int> = scope.async(testDispatcher) {
            delay(1_000)
            throw RuntimeException("oops(❌)") // exposed exception
        }.onCompletion("deferred")

        val result = deferred.await()
    }

    @Test
    fun `direct child of scope - what a surprise 😱`() =
        runTest {
            onCompletion("runTest")

            val scope = CoroutineScope(Job()).onCompletion("scope")

            val parent: Deferred<Int> = scope.async { // root coroutine
                delay(1_000)
                throw RuntimeException("Oops(❌)") // exposed exception
            }.onCompletion("parent")

            // What will happen to this sibling?
            val sibling = scope.launch {
                delay(1_500)
                log("unreachable sibling done")
            }.onCompletion("sibling")

            // Comment out the entire try block and see whether exception still happens.
            try {
                parent.await()
            } catch (ex: Exception) {
                log("Caught: $ex")
            }

            sibling.join()
        }

    ///////////////////////////////////////////////////////////////////////////////////
    // For Root Coroutines
    // - Direct child of `supervisorScope`
    ///////////////////////////////////////////////////////////////////////////////////

    @Test
    fun `direct child of supervisorScope`() =
        runTest {
            onCompletion("runTest")

            supervisorScope {
                onCompletion("supervisorScope")

                // `launch` will make test fail, but `async` will not!
                val deferred = async(ehandler) { // root coroutine, ehandler of no use
                    delay(1_000)
                    throw RuntimeException("Oops(❌)") // exposed exception
                }.onCompletion("child")

                launch {
                    delay(1_500)
                    log("sibling done")
                }.onCompletion("sibling")

                // Uncomment the `try-catch` and see what happens.
                try {
                    deferred.await()
                } catch (ex: Exception) {
                    log("Caught: $ex")
                }
            }
        }

    /**
     * Quiz: What will happen to the `parent` and why?
     */
    @Test
    fun `root coroutine - exposed exception - another example`() = runTest {
        onCompletion("runTest")

        val scope = CoroutineScope(Job() + ehandler).onCompletion("scope")

        val parent = scope.launch { // root coroutine
            val deferred: Deferred<Unit> = scope.async { // root coroutine, too
                delay(100)
                throw RuntimeException("Oops(❌)")
            }.onCompletion("child")

            try {
                deferred.await()
            } catch (ex: Exception) {
                log("Caught: $ex")
            }
            delay(1_000)
        }.onCompletion("parent")

        parent.join()
    }
}