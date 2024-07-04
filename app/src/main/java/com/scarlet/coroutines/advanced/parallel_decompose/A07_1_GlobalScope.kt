package com.scarlet.coroutines.advanced.parallel_decompose

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

/**
 * GlobalScope demo - Not Recommended.
 */

@DelicateCoroutinesApi
private suspend fun loadAndCombine(name1: String, name2: String): Image {
    val apple = GlobalScope.async { loadImage(name1) }.onCompletion("apple")
    val kiwi = GlobalScope.async { loadImage(name2) }.onCompletion("kiwi")
    return combineImages(apple.await(), kiwi.await())
}

@DelicateCoroutinesApi
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

@DelicateCoroutinesApi
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

@DelicateCoroutinesApi
object Even_If_One_Of_Children_Fails_Other_Child_Still_Runs1 {

    @DelicateCoroutinesApi
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

@DelicateCoroutinesApi
object Even_If_One_Of_Children_Fails_Other_Child_Still_Runs2 {

    @DelicateCoroutinesApi
    private suspend fun loadAndCombineFail(name1: String, name2: String): Image {
        // Root coroutines because, they are created via GlobalScope
        val apple = GlobalScope.async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = GlobalScope.async { loadImage(name2) }.onCompletion("kiwi")

        // Exposed exceptions have no effect except applying SC unless called `await`
        return Image("Fake Image")
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

@DelicateCoroutinesApi
object Even_If_One_Of_Children_Fails_Other_Child_Still_Runs3 {

    @DelicateCoroutinesApi
    private suspend fun loadAndCombineFail(name1: String, name2: String): Image {
        // Root coroutines because, they are created via GlobalScope
        val apple = GlobalScope.async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = GlobalScope.async { loadImage(name2) }.onCompletion("kiwi")

        return combineImages(apple.await(), kiwi.await()) // Exception will be thrown here
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = GlobalScope.launch {
            image = loadAndCombineFail("apple", "kiwi")
            log("parent done.") // <== unreachable!
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image").also {
            delay(1_000) // To check what happens to children
        }
    }
}

@DelicateCoroutinesApi
object Even_If_One_Of_Children_Fails_Other_Child_Still_Runs4 {

    @DelicateCoroutinesApi
    private suspend fun loadAndCombineFail(name1: String, name2: String): Image {
        // Root coroutines because, they are created via GlobalScope
        val apple = GlobalScope.async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = GlobalScope.async { loadImage(name2) }.onCompletion("kiwi")

        val image = try {
            combineImages(apple.await(), kiwi.await()) // Exception will be thrown here
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

@DelicateCoroutinesApi
object Even_If_One_Of_Children_Fails_Other_Child_Still_Runs5 {

    @DelicateCoroutinesApi
    private suspend fun loadAndCombineFail(name1: String, name2: String): Image {
        // Root coroutines because, they are created via GlobalScope
        val apple = GlobalScope.async { loadImageFail(name1) }.onCompletion("apple")
        val kiwi = GlobalScope.async { loadImage(name2) }.onCompletion("kiwi")

        return combineImages(apple.await(), kiwi.await()) // Exception will be thrown here
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        onCompletion("runBlocking")
        var image: Image? = null

        val parent = GlobalScope.launch {
            try {
                image = loadAndCombineFail("apple", "kiwi")
                log("parent done.")
            } catch (e: Exception) {
                log("parent caught $e")
                image = Image("Oops")
            }
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image").also {
            delay(1_000) // To check what happens to children
        }
    }
}
