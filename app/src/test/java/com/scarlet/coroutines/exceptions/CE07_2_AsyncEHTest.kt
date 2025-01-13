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
    fun `non-root coroutines - exception propagates`() = runTest {
        onCompletion("runTest")

        val deferred: Deferred<Int> = async { // non root coroutine
            delay(1_000)
            throw RuntimeException("oops(❌)") // Exception will be thrown at this point, and propagate to parent
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
    fun `non-root coroutine, coroutineScope - exception propagates`() = runTest {
        onCompletion("runTest")

        coroutineScope {
            val deferred: Deferred<Int> = async { // non root coroutine
                throw RuntimeException("oops(❌)")
            }.onCompletion("deferred")

            // Unlike documentation saying it useless,
            // exceptions covered, but not considered as handled!!! <-- another surprise!😱
            try {
                deferred.await()
            } catch (ex: Exception) {
                log("Caught: $ex") // Covered, but not considered as handled
            }
        }
    }

    /**
     * **Review of Coroutine Exception Handler (CEH)**
     *
     * - CEH can handle only _uncaught propagated exceptions_.
     *      - Only `launch` propagated exceptions are considered.
     *      - CEH installed in `launch` root coroutines take effect.
     *      - But, CEH installed in `async` root coroutines _has no effect_ at all!
     * - CEH should be installed in either _scopes_ or the _root coroutines_.
     */
    private val ehandler = CoroutineExceptionHandler { context, exception ->
        log("Global CEH: Caught $exception, and handled in $context")
    }

    @Test
    fun `CEH of no use - since async1`() = runTest {
        onCompletion("runTest")

        val scope = CoroutineScope(Job() + ehandler).onCompletion("scope")

        scope.async(ehandler + testDispatcher) { // non root coroutine
            delay(1_000)
            throw RuntimeException("oops(❌)")
        }.onCompletion("child")
    }

    @Test
    fun `CEH of no use - since async2`() = runTest {
        onCompletion("runTest")

        val scope = CoroutineScope(Job() + ehandler).onCompletion("scope")

        scope.async(ehandler + testDispatcher) { // non root coroutine
            async {
                delay(1_000)
                throw RuntimeException("oops(❌)")
            }.onCompletion("child")
        }.onCompletion("parent")
    }

    /**
     * `superVisorScope` does not seem to propagate async coroutine's exceptions 🤬🤬🤬.
     *
     * So, `runTest` renders the test pass.
     */
    @Test
    fun `CEH of no use - since async3`() = runTest {
        onCompletion("runTest")

        supervisorScope {
            onCompletion("supervisorScope")

            val res = async(ehandler) {
                val deferred: Deferred<Int> = async {
                    throw RuntimeException("oops(❌)")
                }.onCompletion("child")

                try {
                    deferred.await()
                } catch (ex: Exception) {
                    log("Caught: $ex") // Covered, but not considered as handled
                }
            }.onCompletion("root coroutine")

            try {
                res.await()
            } catch (ex: Exception) {
                log("Root Coroutine: Caught: $ex") // Exception handled
            }
        }
    }

    @Test
    fun `CEH of no use - since async4`() = runTest {
        onCompletion("runTest")

        supervisorScope {
            onCompletion("supervisorScope")

            val deferred: Deferred<Int> = async(ehandler) { // root coroutine
                delay(1000)
                throw RuntimeException("oops(❌)")
            }.onCompletion("child")

            launch {
                delay(1500)
                log("sibling done")
            }.onCompletion("sibling")

            try {
                deferred.await() // Exception will be thrown at this point
            } catch (ex: Exception) {
                log("Caught: $ex")
            }
        }
    }

    // Another surprise!😱 - Even if CEH takes effect, Job is cancelled.
    @Test
    fun `CEH installed in scope catches only uncaught propagated launch exceptions`() = runTest {
        onCompletion("runTest")
        val scope = CoroutineScope(Job() + ehandler).onCompletion("scope")

        val launchJob = scope.launch {// CEH here also cancel parent job
            val deferred: Deferred<Int> = async {
                delay(100)
                throw RuntimeException("oops(❌)")
            }.onCompletion("child")

            try {
                deferred.await()
            } catch (ex: Exception) {
                log("Caught: $ex")
            }
        }.onCompletion("root coroutine")

        launchJob.join()
    }

    @Test
    fun `CEH installed in launch root coroutine takes effect`() = runTest {
        onCompletion("runTest")

        supervisorScope {
            onCompletion("supervisorScope")

            launch(ehandler) { // root coroutine
                val deferred: Deferred<Int> = async {
                    throw RuntimeException("oops(❌)")
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


