package com.scarlet.coroutines.advanced

import com.scarlet.util.coroutineInfo
import com.scarlet.util.log
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.Executors
import kotlin.random.Random

/**
 * Dispatchers:
 *      1. Dispatchers.Main
 *      2. Dispatchers.IO
 *      3. Dispatchers.Default
 *      4. Dispatchers.Unconfined (not recommended)
 */

/**
 * Exception in thread "main @coroutine#1" java.lang.IllegalStateException:
 * Module with the Main dispatcher had failed to initialize.
 */
object Dispatchers_Main_Failure_Demo {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val scope = CoroutineScope(Job() + Dispatchers.Main)

        scope.launch {
            log("Hello from child coroutine")
            delay(1_000)
        }.join()

        log("Done.")
    }
}

/**
 * The Kotlin Coroutines framework uses the `Default` dispatcher as the default `CoroutineDispatcher`.
 * This means that Kotlin will use the `Default` dispatcher if we don’t specify one explicitly.
 *
 * The `Default` dispatcher is backed by a shared pool of threads. The maximum number of threads used
 * by this dispatcher is equal to the number of CPU cores available to the system
 * (`Runtime.getRuntime().availableProcessors()`) but is always _at least two_.
 *
 * Suitable for CPU-bound tasks that require a lot of computation and benefit from parallelism.
 */
object DefaultDispatchers_Demo {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        log("# processors = ${Runtime.getRuntime().availableProcessors()}")

        repeat(50) {
            launch(Dispatchers.Default) {
                // To make it busy
                val max = List(1_000) { Random.nextInt(1_000) }.maxOrNull()

                log("max = $max")
            }
        }
    }
}

/**
 * By default, the number of threads in the IO dispatcher thread pool is set to either
 * 64 or to the number of CPU cores available to the system, whichever is higher.
 * However, it’s possible to modify the number of threads in the pool by modifying the
 * value of the system property `kotlinx.coroutines.io.parallelism`.
 *
 * Suitable for IO-intensive tasks that involve blocking operations such as reading
 * and writing files, performing database queries, or making network requests.
 */
object IODispatchers_Demo {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        repeat(64) {
            launch(Dispatchers.IO) {
                delay(200)
                log("Running on thread: ${Thread.currentThread().name}")
            }
        }
    }
}

/**
 * IO dispatcher shares threads with a `Dispatchers.Default` dispatcher, so using
 * `withContext(Dispatchers.IO) { ... }` does not lead to an actual switching to another thread.
 */
object ThreadSharing_Demo {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        launch(Dispatchers.Default) {
            log("Default dispatcher: ${Thread.currentThread().name}")

            withContext(Dispatchers.IO) {
                delay(1_000)
                log("IO dispatcher: ${Thread.currentThread().name}")
            }

            log("Default dispatcher: ${Thread.currentThread().name}")
        }
    }
}

/**
 * A coroutine dispatcher that is not confined to any specific thread. It executes
 * the initial continuation of a coroutine in the current call-frame and lets the
 * coroutine resume in whatever thread that is used by the corresponding suspending
 * function, without mandating any specific threading policy.
 *
 * In another words, it executes coroutine immediately on the current thread and later
 * resumes it in whatever thread called `resume`.
 *
 * Nested coroutines launched in this dispatcher form an event-loop to avoid stack
 * overflows.
 */
object Unconfined_Dispatchers_Demo {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        launch(CoroutineName("Main")) {
            coroutineInfo(1)
            withContext(Dispatchers.Unconfined + CoroutineName("Unconfined")) {
//            withContext(CoroutineName("Unconfined")) {
                coroutineInfo(2)

                delay(1_000)
//                someSuspendingFunction(Dispatchers.Default)

                // Whatever thread the suspending function uses will continue to run
                coroutineInfo(2)
            }
            coroutineInfo(1)
        }.join()

        log("Done.")
    }
}

private suspend fun someSuspendingFunction(dispatcher: CoroutineDispatcher) =
    withContext(dispatcher) {
        delay(1_000)
        log("Running on thread: ${Thread.currentThread().name}")
    }

/**
 * `newSingleThreadContext` and `newFixedThreadPoolContext`
 */
@DelicateCoroutinesApi
@ExperimentalCoroutinesApi
object Custom_Dispatchers_Demo {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        val context = newSingleThreadContext("CustomDispatcher 1")
        launch(context) {
            coroutineInfo(0)
            delay(100)
        }.join()
        context.close() // make sure to close

        // Safe way
        newSingleThreadContext("CustomDispatcher 2").use { ctx ->
            launch(ctx) {
                coroutineInfo(0)
            }.join()
        }

        val context1 = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        launch(context1) {
            coroutineInfo(0)
        }.join()
        context1.close() // make sure to close

        /* TODO */
        // Use `use` to safely close the pool
    }

}

/**
 * **Homework**: Please check `limitedParallelism` function for yourself.
 * https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-dispatcher/limited-parallelism.html
 *
 *  `fun limitedParallelism(parallelism: Int): CoroutineDispatcher`
 */
object limitedParallelism_Demo {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val limitedDispatcher = Dispatchers.IO.limitedParallelism(4)

        repeat(20) {
            launch(limitedDispatcher) {
                log("Running on thread: ${Thread.currentThread().name}")
            }
        }
    }
}