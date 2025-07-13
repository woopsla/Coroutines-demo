package com.scarlet.coroutines.exceptions

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import com.scarlet.util.testDispatcher
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

///////////////////////////////////////////////////////////////////////////////////
// For Non-Root Coroutines
///////////////////////////////////////////////////////////////////////////////////

class AsyncEH02Test {

    @Test
    fun `non-root coroutines - exception propagates regardless of await() called or not`() =
        runTest {
            onCompletion("runTest")

            val deferred: Deferred<Int> = async { // non root coroutine
                delay(1_000)
                throw RuntimeException("Oops(❌)") // Exception will be thrown at this point, and propagate to parent
            }.onCompletion("deferred")

            // Unlike documentation saying it useless,
            // exceptions covered, but not considered as handled!!! <-- another surprise!😱
            try {
                deferred.await()
            } catch (ex: Exception) {
                log("Caught: $ex") // Covered, but not considered as handled
            }
        }

    @Test
    fun `non-root coroutine propagates, coroutineScope rethrows`() = runTest {
        onCompletion("runTest")

        coroutineScope {
            val deferred: Deferred<Int> = async { // non root coroutine
                delay(1_000)
                throw RuntimeException("Oops(❌)")
            }.onCompletion("deferred")

            try {
                deferred.await()
            } catch (ex: Exception) {
                log("Caught: $ex") // Covered, but not considered as handled
            }
        }
    }

    //////////////////////////////////////////////////////////
    // Review of Coroutine Exception Handler (CEH)
    //////////////////////////////////////////////////////////

    private val ehandler = CoroutineExceptionHandler { context, exception ->
        log("Global CEH: Caught $exception, and handled in $context")
    }

    @Test
    fun `CEH of no use - handler in scope, since async`() = runTest {
        onCompletion("runTest")

        val scope = CoroutineScope(Job() + ehandler).onCompletion("scope")

        val deferred = scope.async(testDispatcher) { // non root coroutine
            delay(1_000)
            throw RuntimeException("Oops(❌)")
        }.onCompletion("child")
    }

    @Test
    fun `CEH of no use - handler in root coroutine, since async2`() = runTest {
        onCompletion("runTest")

        val scope = CoroutineScope(Job()).onCompletion("scope")

        val deferred = scope.async(ehandler + testDispatcher) { // root coroutine
            async {// non root coroutine
                delay(1_000)
                throw RuntimeException("Oops(❌)")
            }.onCompletion("child")
        }.onCompletion("parent")
    }

    /**
     * Mixed use of `launch` and `async` with CEH installed
     * - Even if CEH takes effect, Job is cancelled.
     */
    @Test
    fun `Mixed use - CEH installed in scope`() = runTest {
        onCompletion("runTest")
        val scope = CoroutineScope(Job() + ehandler).onCompletion("scope")

        val launchJob = scope.launch {// root coroutine
            val deferred: Deferred<Int> = async { // non root coroutine
                delay(100)
                throw RuntimeException("oops(❌)")
            }.onCompletion("child")

            try {
                deferred.await()
            } catch (ex: Exception) {
                log("Caught: $ex") // Covered, but not considered as handled
            }
        }.onCompletion("root coroutine")

        launchJob.join()
    }

    @Test
    fun `Mixed use - CEH installed in root coroutine`() = runTest {
        onCompletion("runTest")

        supervisorScope {
            onCompletion("supervisorScope")

            launch(ehandler) { // root coroutine
                val deferred: Deferred<Int> = async {// non root coroutine
                    throw RuntimeException("Oops(❌)")
                }.onCompletion("child")

                try {
                    deferred.await()
                } catch (ex: Exception) {
                    log("Caught: $ex") // Covered, but not considered as handled
                }
            }.onCompletion("root coroutine")
        }
    }
}


