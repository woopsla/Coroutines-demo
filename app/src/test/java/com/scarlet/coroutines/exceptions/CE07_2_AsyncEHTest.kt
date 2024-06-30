package com.scarlet.coroutines.exceptions

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Non-Root Coroutines Cases
 */
class AsyncEH02Test {

    @Test
    fun `non-root coroutines - exception propagates`() = runTest {
        onCompletion("runTest")

        val deferred: Deferred<Int> = async { // non root coroutine
            delay(1_000)
            throw RuntimeException("Oops!") // Exception will be thrown at this point, and propagate to parent
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
                throw RuntimeException("Oops!")
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
     * **Coroutine Exception Handler (CEH)**
     *
     * - CEH can handle only _uncaught propagated exceptions_.
     *      - Only `launch` propagated exceptions are considered.
     * - CEH should be installed in either _scopes_ or the _root coroutines_.
     * - CEH installed in `launch` root coroutines take effect.
     * - But, CEH installed in `async` root coroutines _has no effect_ at all!
     */
    private val ehandler = CoroutineExceptionHandler { context, exception ->
        log("Global CEH: Caught $exception, and handled in $context")
    }

    @Test
    fun `CEH of no use - since non root coroutine1`() = runTest {
        onCompletion("runTest")

        async(ehandler) { // non root coroutine
            delay(1_000)
            throw RuntimeException("my exception")
        }.onCompletion("child")
    }

    @Test
    fun `CEH of no use - since non root coroutine2`() = runTest {
        onCompletion("runTest")

        async(ehandler) { // non root coroutine
            async {
                delay(1_000)
                throw RuntimeException("my exception")
            }.onCompletion("child")
        }.onCompletion("parent")
    }

    /**
     * `superVisorScope` does not seem to propagate async coroutine's exceptions 🤬🤬🤬.
     *
     * So, `runTest` make the test pass.
     */
    @Test
    fun `CEH in async root coroutines has no effect at all1`() = runTest {
        onCompletion("runTest")

        supervisorScope {
            onCompletion("supervisorScope")

            val res = async(ehandler) {
                val deferred: Deferred<Int> = async {
                    throw RuntimeException("Oops!")
                }.onCompletion("child")

//                try {
//                    deferred.await()
//                } catch (ex: Exception) {
//                    log("Caught: $ex") // Covered, but not considered as handled
//                }
            }.onCompletion("root coroutine")

//            try {
//                res.await()
//            } catch (ex: Exception) {
//                log("Root Coroutine: Caught: $ex") // Exception handled
//            }
        }
    }

    @Test
    fun `CEH in async root coroutines has no effect at all2`() = runTest {
        onCompletion("runTest")

        supervisorScope {
            onCompletion("supervisorScope")

            val deferred: Deferred<Int> = async(ehandler) { // root coroutine
                delay(1000)
                throw RuntimeException("my exception")
            }.onCompletion("child")

            launch {
                delay(1500)
                log("sibling done")
            }.onCompletion("sibling")

//            try {
            deferred.await() // Exception will be thrown at this point
//            } catch (ex: Exception) {
//                log("Caught: $ex")
//            }
        }
    }

    @Test
    fun `CEH is of no use for async coroutine propagated exceptions`() = runTest {
        onCompletion("runTest")
        val scope = CoroutineScope(Job() + ehandler).onCompletion("scope")

        val deferred = scope.async { // root coroutine
            delay(1_000)
            throw RuntimeException("Oops!")
        }.onCompletion("child")

//        try {
        deferred.await() // Exception will be thrown at this point
//        } catch (ex: Exception) {
//            log("Caught: $ex")
//        }
    }

    @Test
    fun `CEH installed in scope catches only uncaught propagated launch exceptions1`() = runTest {
        onCompletion("runTest")
        val scope = CoroutineScope(Job() + ehandler).onCompletion("scope")

        val job1 = scope.async { // root coroutine
            val job2: Deferred<Int> = async {
                delay(100)
                throw RuntimeException("Oops!")
            }.onCompletion("child")

//            try {
//                job2.await()
//            } catch (ex: Exception) {
//                log("Caught: $ex")
//            }
        }.onCompletion("Root coroutine")

//        try {
        job1.await()
//        } catch (ex: Exception) {
//            log("Root coroutine: Caught: $ex") // Exception handled
//        }
    }

    @Test
    fun `CEH installed in scope catches only uncaught propagated launch exceptions2`() = runTest {
        onCompletion("runTest")
        val scope = CoroutineScope(Job() + ehandler).onCompletion("scope")

        val launchJob = scope.launch {
            val deferred: Deferred<Int> = async {
                delay(100)
                throw RuntimeException("Oops!")
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
                    throw RuntimeException("Oops!")
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


