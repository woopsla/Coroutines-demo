package com.scarlet.coroutines.advanced

import com.scarlet.util.coroutineInfo
import com.scarlet.util.delim
import com.scarlet.util.log
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * CoroutineContext:
 *  1. Coroutine Job
 *  2. Coroutine Dispatcher
 *  3. Coroutine Exception Handler
 *  4. Coroutine Name
 *  5. Coroutine Id (Only if debug mode is ON: -Dkotlinx.coroutines.debug)
 */

object CoroutineContext_01 {
    @ExperimentalStdlibApi
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        log(Thread.currentThread().name)
        log("CoroutineContext  = $coroutineContext")
        log("Name              = ${coroutineContext[CoroutineName]}")
        log("Job               = ${coroutineContext[Job]}")
        log("Dispatcher        = ${coroutineContext[ContinuationInterceptor]}")
        log("Dispatcher        = ${coroutineContext[ContinuationInterceptor] as CoroutineDispatcher}")
        log("Dispatcher        = ${coroutineContext[CoroutineDispatcher]}")
        log("Exception handler = ${coroutineContext[CoroutineExceptionHandler]}")
    }
}

object CoroutineContext_Creation_Plus {
    @JvmStatic
    fun main(args: Array<String>) {
//        val context: CoroutineName = CoroutineName("My Coroutine")
//        val context: CoroutineContext.Element = CoroutineName("My Coroutine")
        var context: CoroutineContext = CoroutineName("My Coroutine")
        log(context)

        context += Dispatchers.Default
        log(context)

        context += Job()
        log(context)
    }
}

object CoroutineContext_Merge {
    @JvmStatic
    fun main(args: Array<String>) {
        var context = CoroutineName("My Coroutine") + Dispatchers.Default + Job()
        log(context)
        delim()

        /*
         * Element on the right overrides the same element on the left.
         */

        context += CoroutineName("Your Coroutine")
        log(context)

        context += Dispatchers.IO + SupervisorJob()
        log(context)
        delim()

        /*
         * Empty CoroutineContext
         */

        val emptyContext = EmptyCoroutineContext

        context += emptyContext

        log(context)
        delim()

        /*
         * Minus Key demo
         */

        context = context.minusKey(ContinuationInterceptor)

        log(context)
        delim()
    }
}

object CoroutineContext_Fold {
    @JvmStatic
    fun main(args: Array<String>) {
        val context = CoroutineName("My Coroutine") + Dispatchers.Default + Job()

        context.fold("") { acc, elem ->
            "$acc : $elem"
        }.also(::println)

        context.fold(emptyList<CoroutineContext>()) { acc, elem ->
            acc + elem
        }.joinToString().also(::println)
    }
}

object CoroutineContext_ContextInheritance_Demo {

    @JvmStatic
    fun main(args: Array<String>) {
        log("top-level thread = ${Thread.currentThread().name}")

        // The default context is an event loop on the current thread.
        runBlocking(CoroutineName("Parent Coroutine: runBlocking")) {
            coroutineInfo(1)

            // Inherits context from parent scope. If no inherited dispatcher, use Dispatchers.DEFAULT.
            // launch(CoroutineName("Child Coroutine: launch") + Dispatchers.Default) {
            launch {
                coroutineInfo(2)
                delay(1_000)
            }.join()

            log("runBlocking: try to exit runBlocking")
        }
        log("Bye main")
    }
}
