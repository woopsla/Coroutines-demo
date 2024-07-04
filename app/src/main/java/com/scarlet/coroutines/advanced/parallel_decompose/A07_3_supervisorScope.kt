package com.scarlet.coroutines.advanced.parallel_decompose

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

/**
 * Working solution 2: Use `supervisorScope()`.
 *
 * Because the cancellation process is now straightforward, let's ignore it for now.
 */

object Using_supervisorScope_and_when_child_failed1 {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = supervisorScope {
        // Root coroutines
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        delay(1_000) // Not calling `await`

        // Exception will be thrown when calling `await`
        Image("Fake Image")
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine("apple", "kiwi")
            log("Parent done.")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Using_supervisorScope_and_when_child_failed2 {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = supervisorScope {
        // Root coroutines
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

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
            log("Parent done.")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Using_supervisorScope_and_when_child_failed3 {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = supervisorScope {
        // Root coroutines
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        // Exception will be thrown when calling `await`, and
        // will be rethrown by the supervisorScope unless caught here.
        try {
            combineImages(apple.await(), kiwi.await())
        } catch (e: Exception) {
            log("supervisorScope caught $e")
            Image("Oops")
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine("apple", "kiwi")
            log("Parent done.")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Using_supervisorScope_and_when_child_failed4 {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = supervisorScope {
        // Root coroutines
        val apple = async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = async { loadImage(name2) }.onCompletion("kiwi")

        // Exception will be thrown when calling `await`, and
        // will be rethrown by the supervisorScope unless caught here.
        combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = launch {
            try {
                image = loadAndCombine("apple", "kiwi")
                log("Parent done.")
            } catch (e: Exception) {
                log("parent caught $e")
                image = Image("Oops")
            }
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}