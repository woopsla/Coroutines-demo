package com.scarlet.coroutines.advanced

import com.scarlet.util.coroutineInfo
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing
import java.lang.RuntimeException
import java.util.concurrent.Executors

/**
 * **Coroutine Scope Functions**
 *
 * Unlike `async` or `launch`, the body of `coroutineScope` is called *in-place*.
 * It formally creates a new coroutine, but it suspends the previous one until the new
 * one is finished, so it **does not start any concurrent process**.
 *
 * The provided scope inherits its `coroutineContext` from the outer scope, but overrides
 * the context's `Job`. This way, the produced scope respects parental responsibilities:
 *  - inherits a context from its parent,
 *  - awaits for all children before it can finish itself,
 *  - cancels all its children, when the parent is canceled.
 *
 * 1. coroutineScope
 * 2. supervisorScope
 * 3. withContext
 * 4. withTimeout
 * 5. withTimeoutOrNull
 */

object coroutineScope_Demo1 {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        log("runBlocking: $coroutineContext")

        val a = coroutineScope {
            delay(1_000).also {
                log("a: $coroutineContext")
            }
            10
        }
        log("a is calculated")
        val b = coroutineScope {
            delay(1_000).also {
                log("b: $coroutineContext")
            }
            20
        }
        log("a = $a, b = $b")
    }
}

object coroutineScope_Demo2 {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        log("runBlocking begins")

        coroutineScope {
            log("Launching children ...")

            launch {
                log("child1 starts")
                delay(2_000)
            }.onCompletion("child1")

            launch {
                log("child2 starts")
                delay(1_000)
            }.onCompletion("child2")

            delay(10)

            log("Waiting until children are completed ...")
        }

        log("Done!")
    }
}

object coroutineScope_Demo3 {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        log("runBlocking begins")

        try {
            coroutineScope {
                log("Launching children ...")

                launch {
                    log("child1 starts")
                    delay(2_000)
                }.onCompletion("child1")

                launch {
                    log("child2 starts")
                    delay(1_000)
                    throw RuntimeException("Oops")
                }.onCompletion("child2")

                delay(10)

                log("Waiting until children are completed ...")
            }
        } catch (ex: Exception) {
            log("Caught exception: ${ex.javaClass.simpleName}")
        }

        log("Done!")
    }
}

/**/

data class Details(val name: String, val followers: Int)
data class Tweet(val text: String)

class ApiException(val code: Int, message: String) : Throwable(message)

private suspend fun getFollowersNumber(): Int {
    delay(100)
    throw ApiException(500, "Service unavailable")
}

private suspend fun getUserName(): String {
    delay(500)
    return "paula abdul"
}

private suspend fun getTweets(): List<Tweet> {
    delay(500)
    return listOf(Tweet("Hello, world"))
}

object Not_What_We_Want {

    private suspend fun getUserDetails(scope: CoroutineScope): Details {
        val userName = scope.async { getUserName() }
        val followersNumber = scope.async { getFollowersNumber() }

        return Details(userName.await(), followersNumber.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val details = try {
            getUserDetails(this)
        } catch (e: ApiException) {
            log("Error: ${e.code}")
            null
        }
        log("User: $details")
        val tweets = async { getTweets() }
        log("Tweets: ${tweets.await()}")
    }
// Only Exception...
}

object What_We_Want {

    private suspend fun getUserDetails(): Details = coroutineScope {
        val userName = async { getUserName() }
        val followersNumber = async { getFollowersNumber() }

        Details(userName.await(), followersNumber.await())
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val details = try {
            getUserDetails()
        } catch (e: ApiException) {
            log("Error: ${e.code}")
            null
        }
        val tweets = async { getTweets() }
        log("User: $details")
        log("Tweets: ${tweets.await()}")
    }
// User: null
// Tweets: [Tweet(text=Hello, world)]
}

object withContext_Demo {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val parent = launch(CoroutineName("parent")) {
            coroutineInfo(0)

            coroutineScope {
                coroutineContext.job.onCompletion("coroutineScope")
                log("\t\tInside coroutineScope")
                coroutineInfo(1)
                delay(100)
            }

            withContext(CoroutineName("child 1") + Dispatchers.Default) {
                coroutineContext.job.onCompletion("withContext")
                log("\t\tInside first withContext")
                coroutineInfo(1)
                delay(500)
            }

            Executors.newFixedThreadPool(3).asCoroutineDispatcher().use { ctx ->
                withContext(CoroutineName("child 2") + ctx) {
                    coroutineContext.job.onCompletion("newFixedThreadPool")
                    log("\t\tInside second withContext")
                    coroutineInfo(1)
                    delay(1_000)
                }
            }
        }.onCompletion("parent")

        delay(50)
        log("children after 50ms  = ${parent.children.toList()}")
        delay(200)
        log("children after 250ms = ${parent.children.toList()}")
        delay(600)
        log("children after 850ms = ${parent.children.toList()}")
        parent.join()
    }
}

object MainSafety_Demo {

    private suspend fun fibonacci(n: Long): Long =
        withContext(Dispatchers.Default) {
            log(coroutineContext)
            fib(n)
        }

    private fun fib(n: Long): Long = if (n == 0L || n == 1L) n else fib(n - 1) + fib(n - 2)

    @JvmStatic
    fun main(args: Array<String>) = runBlocking(CoroutineName("parent") + Dispatchers.Swing) {
        log(coroutineContext)

        val job1 = launch {
            log("fib(44) = ${fibonacci(44)}")
        }

        val job2 = launch {
            for (i in 1..10) {
                log("i = $i")
                delay(500)
            }
        }

        joinAll(job1, job2)
        log("Done")
    }
}

object Timeout {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        launch {
            launch { // will be cancelled by its parent
                delay(2_000)
                log("Will not be printed")
            }
            withTimeout(1_000) { // we cancel launch
                delay(1_500)
            }
        }.onCompletion("child 1")

        launch {
            delay(2_000)
            log("child2 done")
        }.onCompletion("child 2")
    }
// (2 sec)
// Done
}

object WithTimeoutOrNull_Demo {
    class User

    private suspend fun fetchUser(): User {
        // Runs forever
        while (true) {
            yield()
        }
    }

    private suspend fun getUserOrNull(): User? =
        withTimeoutOrNull(3_000) {
            fetchUser()
        }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val user = getUserOrNull()
        log("User: $user")
    }
// (3 sec)
// User: null
}
