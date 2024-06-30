package com.scarlet.util

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.coroutines.ContinuationInterceptor

val log: Logger = LoggerFactory.getLogger("Coroutines")

fun log(msg: Any?) {
    log.info(msg.toString())
}

fun delim(char: String = "-", length: Int = 50) {
    log(char.repeat(length))
}

fun spaces(level: Int) = "\t".repeat(level)

fun CoroutineScope.coroutineInfo(indent: Int) {
    delim()
    log("\t".repeat(indent) + "thread = ${Thread.currentThread().name}")
    log("\t".repeat(indent) + "job = ${coroutineContext[Job]}")
    log("\t".repeat(indent) + "dispatcher = ${coroutineContext[ContinuationInterceptor]}")
    log("\t".repeat(indent) + "name = ${coroutineContext[CoroutineName]}")
    log("\t".repeat(indent) + "handler = ${coroutineContext[CoroutineExceptionHandler]}")
    delim()
}

@ExperimentalStdlibApi
fun scopeInfo(scope: CoroutineScope, indent: Int) {
    delim()
    log("\t".repeat(indent) + "Scope's job = ${scope.coroutineContext[Job]}")
    log("\t".repeat(indent) + "Scope's dispatcher = ${scope.coroutineContext[CoroutineDispatcher]}")
    log("\t".repeat(indent) + "Scope's name = ${scope.coroutineContext[CoroutineName]}")
    log("\t".repeat(indent) + "Scope's handler = ${scope.coroutineContext[CoroutineExceptionHandler]}")
    delim()
}

/**
 * **Caveat**: should be cautiously used because it shows the status of the coroutine
 * at the time of invocation, not when completed.
 */
fun Job.completeStatus(name: String = "Job", level: Int = 0) = apply {
    log("${spaces(level)}$name: isCancelled = $isCancelled")
}

/**
 * **Caveat**: should be cautiously used because it shows the status of the coroutine
 * at the time of invocation, not when completed.
 */
fun CoroutineScope.completeStatus(name: String = "scope", level: Int = 0) = apply {
    log("${spaces(level)}$name: isCancelled = ${coroutineContext.job.isCancelled}")
}

fun CoroutineScope.onCompletion(name: String): CoroutineScope = apply {
    coroutineContext.job.invokeOnCompletion {
        log("$name: isCancelled = ${coroutineContext.job.isCancelled}, exception = ${it?.javaClass?.name}")
    }
}

fun Job.onCompletion(name: String, level: Int = 0): Job = apply {
    invokeOnCompletion {
        log("${spaces(level)}$name: isCancelled = $isCancelled, exception = ${it?.javaClass?.name}")
    }
}

fun <T> Deferred<T>.onCompletion(name: String, level: Int = 0): Deferred<T> = apply {
    invokeOnCompletion {
        log("${spaces(level)}$name: isCancelled = $isCancelled, exception = ${it?.javaClass?.name}")
    }
}
