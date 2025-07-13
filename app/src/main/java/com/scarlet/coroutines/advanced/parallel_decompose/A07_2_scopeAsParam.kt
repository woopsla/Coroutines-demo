package com.scarlet.coroutines.advanced.parallel_decompose

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

/**
 * Working solution 1: Pass the parent coroutine scope as Parameter.
 */

object Passing_Coroutine_Scope_As_Parameter_Works_But_Not_Recommended {

    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        val apple = scope.async { loadImage(name1) }.onCompletion("apple")
        val kiwi = scope.async { loadImage(name2) }.onCompletion("kiwi")

        log("Waiting for two images to combine")
        return combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine(this, "apple", "kiwi")
            log("parent done: image = $image")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Parent_Cancellation_When_Passing_Coroutine_Scope_As_Parameter {

    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        val apple = scope.async { loadImage(name1) }.onCompletion("apple")
        val kiwi = scope.async { loadImage(name2) }.onCompletion("kiwi")

        log("Waiting for two images to combine")
        return combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine(this, "apple", "kiwi")
            log("Parent done: image = $image")
        }.onCompletion("parent")

        delay(200)
        parent.cancel(CancellationException("Cancel parent coroutine after 500ms"))
        parent.join()

        log("combined image = $image").also {
            delay(1_000) // To check what happens to children just in case
        }
    }
}

object Child_Failure_When_Passing_Coroutine_Scope_As_Parameter {

    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        // Are these Root Coroutines because they are created through `scope`?
        // If not root coroutines, try-catch around `await()` is eventually useless.
        val apple = scope.async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = scope.async { loadImage(name2) }.onCompletion("kiwi")

        log("Waiting for two images to combine")
        // Documentation says try-catch is useless here in case of non-root coroutines.
        // However, it is executed anyway.
        // You'd better think of it as covered, but not treated as handled ... [by Jungsun Kim]
        val image = try {
            combineImages(
                apple.await(),
                kiwi.await(),
            )
        } catch (e: Exception) { // eventually useless
            log("combineImages: Caught $e")
            if (e is CancellationException) throw e
            Image("Fake Combined Image")
        }
        return image
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            try {
                image = loadAndCombine(this, "apple", "kiwi")
                log("Parent done: image = $image") // is this reachable or not?
            } catch (e: Exception) { // eventually useless
                log("Caught in parent: $e")
                if (e is CancellationException) throw e
                image = Image("Oops") // useless
            }
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image") // <== unreadable!
    }
}
