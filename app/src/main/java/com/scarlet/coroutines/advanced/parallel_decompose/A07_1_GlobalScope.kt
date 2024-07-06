@file:OptIn(DelicateCoroutinesApi::class)

package com.scarlet.coroutines.advanced.parallel_decompose

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

/**
 * GlobalScope demo - Not Recommended.
 */

private suspend fun loadAndCombine(name1: String, name2: String): Image {
    val apple = GlobalScope.async { loadImage(name1) }.onCompletion("apple")
    val kiwi = GlobalScope.async { loadImage(name2) }.onCompletion("kiwi")
    return combineImages(apple.await(), kiwi.await())
}

object Works_But_Not_Recommended {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = GlobalScope.launch {
            image = loadAndCombine("apple", "kiwi")
            log("parent done.")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }
}

object Even_If_Parent_Cancelled_Children_Keep_Going {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = GlobalScope.launch {
            image = loadAndCombine("apple", "kiwi")
            log("parent done.")
        }.onCompletion("parent")

        delay(500)
        log("Cancel parent coroutine after 500ms")
        parent.cancelAndJoin()

        log("combined image = $image").also {
            delay(1_000) // To check what happens to children
        }
    }
}

object Even_If_One_Of_Children_Fails_Other_Child_Still_Runs {

    private suspend fun loadAndCombineFail(name1: String, name2: String): Image {
        val apple = GlobalScope.async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = GlobalScope.async { loadImage(name2) }.onCompletion("kiwi")

        return combineImages(apple.await(), kiwi.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = GlobalScope.launch {
            image = loadAndCombineFail("apple", "kiwi")
            log("parent done.")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image").also {
            delay(1_000) // To check what happens to children
        }
    }
}
