package com.scarlet.coroutines.exceptions

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * **Root Coroutines**
 * - Direct child of scope
 * - Direct child of supervisorScope
 * ```
 *                scope           supervisorScope
 *                  |                   |
 *            root coroutine       root coroutine
 *```
 * ⚠️Jungsun's note:
 * Documentation says the _exposed exception_ will be silently dropped unless
 * `.await()` is called on the deferred value.
 *
 * However, actually it propagates and cancels all siblings and the scope (if `Job()` is used)
 * **without failing the test**.
 * Therefore, structured concurrency still works, but we have a chance to handle
 * exceptions using `try-catch`.
 */
class AsyncEH01Test {

    @Test
    fun `try-catch inside async works normally`() = runTest {
        onCompletion("runTest")

        val deferred = async {
            try {
                throw RuntimeException("my exception")
            } catch (ex: Exception) {
                log("Caught: $ex") // caught
            }
            42
        }.onCompletion("deferred")

        log("result = ${deferred.await()}")
    }

    /**
     * Even though the exception is caught, it propagates to the parent scope.
     * The structured concurrency still works.
     * Test passes because the exception is handled and there are no uncaught exceptions.
     */
    @Test
    fun `direct child of scope - catch on await`() = runTest {
        onCompletion("runTest")

        // Job or SupervisorJob does matter.
        val scope = CoroutineScope(Job()).onCompletion("scope")

        val deferred: Deferred<Int> = scope.async { // root coroutine
            delay(1_000)
            throw RuntimeException("Oops!") // exposed exception
        }.onCompletion("deferred")

        // Uncomment the `try-catch` and see what happens.
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
        val deferred: Deferred<Int> = scope.async {
            delay(1_000)
            throw RuntimeException("Oops!") // exposed exception
        }.onCompletion("deferred")

        // No `.await()` called, check whether exception will be silently dropped.

        deferred.join()
    }

    @Test
    fun `direct child of scope - what a surprise 😱`() =
        runTest {
            onCompletion("runTest")

            val scope = CoroutineScope(Job()).onCompletion("scope")

            val parent: Deferred<Int> = scope.async { // root coroutine
                delay(1_000)
                throw RuntimeException("my exception") // exposed exception
            }.onCompletion("parent")

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

    @Test
    fun `direct child of supervisorScope - catch on await`() =
        runTest {
            onCompletion("runTest")

            supervisorScope {
                onCompletion("supervisorScope")

                // `launch` will make test fail, but `async` will not!
                val deferred = async { // root coroutine
                    delay(100)
                    throw RuntimeException("my exception") // exposed exception
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

    @Test
    fun `direct child of supervisorScope - await not called`() =
        runTest {
            onCompletion("runTest")

            // Unlike `launch`, uncaught propagated exceptions do not propagate to parent, so test passes.
            supervisorScope {
                onCompletion("supervisorScope")

                // `launch` will make test fail, but `async` will not!
                val deferred = async { // root coroutine
                    delay(100)
                    throw RuntimeException("my exception") // exposed exception
                }.onCompletion("child")

                launch {
                    delay(1_500)
                    log("sibling done")
                }.onCompletion("sibling")

                // No `.await()` called, check whether exception will be silently dropped.
            }
        }

    /**
     * Quiz: Why `whoAmI` coroutine cancelled?
     */
    @Test
    fun `root coroutine - exposed exception - another example`() = runTest {
        onCompletion("runTest")

        val scope = CoroutineScope(Job()).onCompletion("scope")

        val job = scope.launch { // root coroutine
            val deferred: Deferred<Int> = scope.async { // root coroutine, too
                delay(100)
                throw RuntimeException("Oops!")
            }.onCompletion("child")

            try {
                deferred.await()
            } catch (ex: Exception) {
                log("Caught: $ex")
            }
            delay(1_000)
        }.onCompletion("whoAmI")

        job.join()
    }
}