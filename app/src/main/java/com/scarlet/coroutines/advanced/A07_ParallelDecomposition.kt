package com.scarlet.coroutines.advanced

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import java.lang.RuntimeException

@JvmInline
private value class Image(val name: String)

private suspend fun loadImage(name: String): Image {
    log("Loading ${name}: started.")
    delay(1_000)
    log("Loading ${name}: done.")
    return Image(name)
}

private suspend fun loadImageFail(name: String): Image {
    log("Loading ${name}: started.")
    delay(500)
    throw RuntimeException("oops")
}

private fun combineImages(image1: Image, image2: Image): Image =
    Image("${image1.name} & ${image2.name}")

/**
 * GlobalScope demo - not recommended.
 */

@DelicateCoroutinesApi
private suspend fun loadAndCombine(name1: String, name2: String): Image {
    val deferred1 = GlobalScope.async { loadImage(name1) }.onCompletion("deferred1")
    val deferred2 = GlobalScope.async { loadImage(name2) }.onCompletion("deferred2")
    return combineImages(deferred1.await(), deferred2.await())
}

@DelicateCoroutinesApi
object GlobalScope_Not_Recommended {
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
object GlobalScope_Even_If_Parent_Cancelled_Children_Keep_Going {
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
        log("combined image = $image")

        delay(1_000) // To check what happens to children
    }
}

@DelicateCoroutinesApi
private suspend fun loadAndCombineFail(name1: String, name2: String): Image {
    val deferred1 = GlobalScope.async { loadImageFail(name1) }.onCompletion("deferred1")
    val deferred2 = GlobalScope.async { loadImage(name2) }.onCompletion("deferred2")

    var image1: Image?
//    try {
        image1 = deferred1.await() /* Actual exception will be thrown at this point! */
        log("image1 = $image1")
//    } catch (e: Exception) {
//        log("deferred1 caught $e")
//    }
    val image2 = deferred2.await()
    log("image2 = $image2")

    return combineImages(image1 ?: Image("Oops"), image2)
}

@DelicateCoroutinesApi
object GlobalScope_EvenIf_One_Of_Children_Fails_Other_Child_Still_Runs {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = GlobalScope.launch {
//            try {
                image = loadAndCombineFail("apple", "kiwi")
//            } catch (e: Exception) {
//                log("parent caught $e")
//            }
            log("parent done.")
        }

        parent.join()
        log("combined image = $image")

        delay(1_000) // To check what happens to children
    }
}

/**
 * Working solution 1: Pass the parent coroutine scope.
 */

object Parent_Cancellation_When_Passing_Coroutine_Scope_As_Parameter {

    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        val deferred1 = scope.async { loadImage(name1) }.onCompletion("deferred1")
        val deferred2 = scope.async { loadImage(name2) }.onCompletion("deferred2")

        return combineImages(deferred1.await(), deferred2.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine(this, "apple", "kiwi")
            log("Parent done")
        }.onCompletion("parent")

        parent.join()
//        delay(500)
//        log("Cancel parent coroutine after 500ms")
//        parent.cancelAndJoin()

        log("combined image = $image")

        delay(1_000) // To check what happens to children just in case
    }
}

object Child_Failure_When_Passing_Coroutine_Scope_As_Parameter {

    private suspend fun loadAndCombine(scope: CoroutineScope, name1: String, name2: String): Image {
        // Exception will be thrown inside `async` block, and will propagate.
        val deferred1 = scope.async { loadImageFail(name1) }.onCompletion("deferred1")
        val deferred2 = scope.async { loadImage(name2) }.onCompletion("deferred2")

        return combineImages(deferred1.await(), deferred2.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine(this, "apple", "kiwi")
            log("Parent done")
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }

}

/**
 * Working solution 2 (Preferable): Use `coroutineScope()`.
 */

object Using_coroutineScope_and_when_parent_cancelled {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = coroutineScope {
        val deferred1 = async { loadImage(name1) }.onCompletion("deferred1")
        val deferred2 = async { loadImage(name2) }.onCompletion("deferred2")
        combineImages(deferred1.await(), deferred2.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = launch {
            image = loadAndCombine("apple", "kiwi")
            log("Parent done.")
        }.onCompletion("parent")

        parent.join()
//        delay(500)
//        log("Cancel parent coroutine after 500ms")
//        parent.cancelAndJoin()

        log("combined image = $image")
    }

}

object Using_coroutineScope_and_when_child_failed {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = coroutineScope {
        // Exception will be thrown inside `async` block, and will rethrow.
        val deferred1 = async { loadImageFail(name1) }.onCompletion("deferred1")
        val deferred2 = async { loadImage(name2) }.onCompletion("deferred2")

        combineImages(deferred1.await(), deferred2.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = launch {
            try {
                image = loadAndCombine("apple", "kiwi")
                log("Parent done.")
            } catch (e: Exception) {
                log("parent caught $e")
            }
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }

}

object Using_supervisorScope_and_when_child_failed {

    private suspend fun loadAndCombine(name1: String, name2: String): Image = supervisorScope {
        val deferred1 = async { loadImageFail(name1) }.onCompletion("deferred1")
        val deferred2 = async { loadImage(name2) }.onCompletion("deferred2")

        // Exception will be thrown inside `await`, and will rethrow if not handled
//        try {
            combineImages(deferred1.await(), deferred2.await())
//        } catch (e: Exception) {
//            log("supervisorScope caught $e")
//            Image("Oops")
//        }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var image: Image? = null

        val parent = launch {
            try {
                image = loadAndCombine("apple", "kiwi")
                log("Parent done.")
            } catch (e: Exception) {
                log("parent caught $e")
            }
        }.onCompletion("parent")

        parent.join()
        log("combined image = $image")
    }

}