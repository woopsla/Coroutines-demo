package com.scarlet.coroutines.advanced

import com.scarlet.util.log
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.contracts.contract
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object SuspendOrigin {
    private suspend fun fooWithDelay(a: Int, b: Int): Int {
        log("step 1")
        myDelay(3_000)
        log("step 2")
        return a + b
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        log("main started")

        log("result = ${fooWithDelay(3, 4)}")

        log("main end")
    }

    private suspend fun myDelay(ms: Long) {
        suspendCoroutine { continuation ->
            executor.schedule({
                continuation.resume(Unit)
            }, ms, TimeUnit.MILLISECONDS)
        }
    }

    private val executor = Executors.newSingleThreadScheduledExecutor {
        Thread(it, "scheduler").apply { isDaemon = true }
    }
}