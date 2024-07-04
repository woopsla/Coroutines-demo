package com.scarlet.coroutines.advanced.parallel_decompose

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

/**
 * Working solution 1: Pass the parent coroutine scope as Parameter.
 */

@DelicateCoroutinesApi
object Passing_Coroutine_Scope_As_Parameter_Works_But_Not_Recommended {
    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        val apple = scope.async { loadImage(name1) }.onCompletion("apple")
        val kiwi = scope.async { loadImage(name2) }.onCompletion("kiwi")

        return combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = GlobalScope.launch {
            image = loadAndCombine(this, "apple", "kiwi")
            log("parent done.")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Parent_Cancellation_When_Passing_Coroutine_Scope_As_Parameter {

    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        val apple = scope.async { loadImage(name1) }.onCompletion("apple")
        val kiwi = scope.async { loadImage(name2) }.onCompletion("kiwi")

        return combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine(this, "apple", "kiwi")
            log("Parent done")
        }.onCompletion("parent")

        delay(500)
        log("Cancel parent coroutine after 500ms")
        parent.cancelAndJoin()

        log("combined image = $image").also {
            delay(1_000) // To check what happens to children just in case
        }
    }
}

object Child_Failure_When_Passing_Coroutine_Scope_As_Parameter1 {

    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        // Are these Root Coroutines because they are created through `scope`?
        // If not root coroutines, exceptions will be thrown inside `async` block, and will propagate.
        val apple = scope.async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = scope.async { loadImage(name2) }.onCompletion("kiwi")

        delay(1_000) // Not calling `await`
        return Image("Fake Image")
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine(this, "apple", "kiwi")
            log("Parent done")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image") // <== unreadable!
    }
}

object Child_Failure_When_Passing_Coroutine_Scope_As_Parameter2 {

    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        // Since not root coroutines, exception will be thrown inside `async` block, and will propagate.
        val apple = scope.async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = scope.async { loadImage(name2) }.onCompletion("kiwi")

        return combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine(this, "apple", "kiwi")
            log("Parent done")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image") // <== unreadable!
    }
}

object Child_Failure_When_Passing_Coroutine_Scope_As_Parameter3 {

    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        // If not root coroutines, exception will be thrown inside `async` block, and will propagate.
        val apple = scope.async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = scope.async { loadImage(name2) }.onCompletion("kiwi")

        // Documentation says try-catch is useless here in case of no root coroutines, but it's not.
        // You should think of it as covered, but not treated as handled ...
        val image = try { // useless
            combineImages(apple.await(), kiwi.await())
        } catch (e: Exception) {
            log("Caught $e")
            Image("Oops")
        }
        return image
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine(this, "apple", "kiwi")
            log("Parent done")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image") // <== unreadable!
    }
}

object Child_Failure_When_Passing_Coroutine_Scope_As_Parameter4 {

    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        // If not root coroutines, exception will be thrown inside `async` block, and will propagate.
        val apple = scope.async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = scope.async { loadImage(name2) }.onCompletion("kiwi")

        // Documentation says try-catch is useless here in case of no root coroutines, but it's not.
        // You should think of it as covered, but not treated as handled ...
        return combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            try {
                image = loadAndCombine(this, "apple", "kiwi")
                log("Parent done")
            } catch (e: Exception) { // useless
                log("Caught $e")
                image = Image("Oops")
            }
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image") // <== unreadable!
    }
}