package com.scarlet.coroutines.advanced.parallel_decompose

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

/**
 * Working solution 3 (Preferable): Use `coroutineScope()`.
 *
 * Because the cancellation process is now straightforward, let's ignore it for now.
 */

object Using_coroutineScope {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = coroutineScope {
        // Root coroutines
        val apple = async { loadImage(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        log("Waiting for two images to combine")
        combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine("apple", "kiwi")
            log("Parent done: image = $image")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Using_coroutineScope_and_when_child_failed1 {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = coroutineScope {
        // Since non-root coroutines, exceptions will be thrown inside `async` block
        // even if we are not calling `await`.
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        delay(1_000) // Not calling `await`

        Image("Fake Combined Image")
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine("apple", "kiwi")
            log("Parent done: image = $image")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Using_coroutineScope_and_when_child_failed2 {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = coroutineScope {
        // Since non-root coroutines, exceptions will be thrown inside `async` block
        // even if we are not calling `await`.
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        log("Waiting for two images to combine")
        combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine("apple", "kiwi")
            log("Parent done: image = $image")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Using_coroutineScope_and_when_child_failed3_right_way {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = coroutineScope {
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        log("Waiting for two images to combine")
        combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            try {
                image = loadAndCombine("apple", "kiwi")
                log("Parent done: image = $image")
            } catch (e: Exception) {
                log("Caught $e in parent")
                image = Image("Oops")
            }
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Using_coroutineScope_and_when_child_failed4 {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = coroutineScope {
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        try {
            combineImages(apple.await(), kiwi.await())
        } catch (e: Exception) { // useless
            log("Caught $e in coroutineScope")
            Image("Oops")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine("apple", "kiwi")
            log("Parent done: image = $image") // <== unreachable
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

