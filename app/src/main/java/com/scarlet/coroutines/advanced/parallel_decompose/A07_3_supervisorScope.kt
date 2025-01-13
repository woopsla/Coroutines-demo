package com.scarlet.coroutines.advanced.parallel_decompose

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

/**
 * Working solution 2: Use `supervisorScope()`.
 *
 * Because the cancellation process is now straightforward, let's ignore it for now.
 */

object Using_supervisorScope {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = supervisorScope {
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

object Using_supervisorScope_and_when_child_failed1_not_calling_await {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = supervisorScope {
        // Root coroutines
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        // Exception is supposed to be thrown here when calling `await`.
        // But we are not calling `await` here to see what happens.
        delay(1_000) // Not calling `await`

        Image("Fake Combined Image")
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

object Using_supervisorScope_and_when_child_failed2_calling_await {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = supervisorScope {
        // Root coroutines
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        log("Waiting for two images to combine")

        // Exception will be thrown when calling `await`, and
        // will be rethrown by the supervisorScope unless caught here.
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

object Using_supervisorScope_and_when_child_failed3_catch_outside_supervisorScope {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = supervisorScope {
        // Root coroutines
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
                if (e is CancellationException) throw e
                image = Image("Oops")
            }
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Using_supervisorScope_and_when_child_failed4_catch_inside_supervisorScope {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = supervisorScope {
        // Root coroutines
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        log(
            "Waiting for two images to combine"
        )
        try {
            combineImages(apple.await(), kiwi.await())
        } catch (e: Exception) {
            log("Caught $e in supervisorScope")
            if (e is CancellationException) throw e
            else Image("Fake Combined Image")
        }
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