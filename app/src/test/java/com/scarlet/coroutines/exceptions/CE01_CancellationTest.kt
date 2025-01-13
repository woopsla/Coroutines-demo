package com.scarlet.coroutines.exceptions

import com.scarlet.util.completeStatus
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CancellationTest {

    private suspend fun networkRequestCooperative() {
        log("networkRequestCooperative (\uD83E\uDEF1\uD83C\uDFFC\u200D\uD83E\uDEF2\uD83C\uDFFE)")
        delay(1_000)
        log("networkRequestCooperative done (\uD83E\uDEF1\uD83C\uDFFC\u200D\uD83E\uDEF2\uD83C\uDFFE)")
    }

    private suspend fun networkRequestFailed() {
        log("networkRequestFailed (❌)")
        delay(1_000)
        log("throwing exception from networkRequestFailed (❌)")
        throw RuntimeException("Oops...from networkRequestFailed (❌)")
    }

    private suspend fun networkRequestUncooperative() = coroutineScope {
        fun fib(n: Long): Long = if (n <= 1) n else fib(n - 1) + fib(n - 2)

        log("networkRequestUncooperative (\uD83D\uDD12)")
        log("I AM BUSY ... NO INTERRUPTIONS PLEASE (⛔)")
        fib(45) // simulating blocking operation
        log("networkRequestUncooperative done (\uD83D\uDD12)")
    }

    /**/

    @Test
    fun `uncooperative coroutine cannot be cancelled`() = runTest {
        val job = launch {
            try {
                networkRequestUncooperative()
            } catch (ex: Exception) {
                log("Caught: ${ex.javaClass.simpleName}")
                if (ex is CancellationException) {
                    throw ex
                }
            }

            log("Am I printed?")

            try {
                delay(100)
            } catch (ex: Exception) {
                log("Caught: ${ex.javaClass.simpleName}")
                if (ex is CancellationException) {
                    throw ex
                }
            }

            log("Am I printed?, too")
        }.onCompletion("job")

        delay(100)

        job.cancelAndJoin()
        job.completeStatus()
    }

    @Test
    fun `cooperative coroutine can be canceled`() = runTest {
        val job = launch {
            try {
                networkRequestCooperative()

                println("Am I printed?")
            } catch (ex: Exception) {
                log("Caught: ${ex.javaClass.simpleName}")
                if (ex is CancellationException) {
                    throw ex
                }
            }
        }.onCompletion("Job")

        delay(100)

        job.cancelAndJoin()
    }

    @Test
    fun `failed suspending function throws causing exception, not cancellation exception`() =
        runTest {
            launch {
                try {
                    networkRequestFailed()
                } catch (ex: Exception) {
                    log("Caught exception = $ex")
                }
            }
        }

    /**
     * Check to see what happens when coroutine is cancelled
     *
     * Cancelled coroutine sets its status as `Cancelling`, and executes
     * rest of the computation (probably call `cancel()` to child coroutines if exist).
     * If `Cancellation` exception is thrown from any computation, it skips
     * the rest of the computation _immediately!_.
     */

    // DO NOT CATCH CANCELLATION EXCEPTION!! IF YOU HAPPEN TO CATCH IT, RETHROW IT!!
    @Test
    fun `cancellation exception swallowed - so, next suspend function starts running`() = runTest {
        val job = launch {
            log("Coroutine starts running ... isActive = $isActive")

            try {
                networkRequestCooperative()
            } catch (ex: Exception) { // CATCH ALL EXCEPTIONS including CancellationException
                log("Caught: ${ex.javaClass.simpleName}")
            }

            log("Coroutine keep running ... isActive = $isActive")
            networkRequestUncooperative() // this will not be skipped
        }.onCompletion("job")

        delay(100)
        job.cancelAndJoin()
    }

    @Test
    fun `cancellation caught, but rethrown - remaining computation all skipped`() = runTest {
        val job = launch {
            try {
                networkRequestCooperative()
            } catch (ex: Exception) {
                log("Caught: ${ex.javaClass.simpleName}")
                if (ex is CancellationException) {
                    throw ex
                }
            }

            log("All subsequent computations will be skipped ...")
            networkRequestUncooperative() // long running computation
            log("This will not be printed")

        }.onCompletion("job")

        delay(100)

        job.cancelAndJoin()
    }

}