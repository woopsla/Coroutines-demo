package com.scarlet.coroutines.basics

import android.annotation.SuppressLint
import com.scarlet.util.log
import com.scarlet.util.spaces
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import java.lang.Thread.sleep
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

data class Item(val value: String)
data class Token(val no: Int)
data class Post(val token: Token, val item: Item)

@JvmInline
private value class Data(val value: Int)

fun uiOnMain(block: () -> Unit) {
    Dispatchers.Swing.asExecutor().execute(block)
}

private fun CoroutineScope.loop() {
    launch {
        repeat(5) {
            log("${spaces(10)}Am I running?")
            delay(500)
        }
    }
}

object UsingSyncCall {

    // Blocking network request code
    private fun requestToken(): Token {
        log("Token request is being processed ...")
        sleep(1_000) // simulate network delay
        log("Token creation done")

        return Token(42)
    }

    // Blocking network request code
    private fun createPost(token: Token, item: Item): Post {
        log("Post creation is being processed ...")
        sleep(1_000) // simulate network delay
        log("Post creation done")

        return Post(token, item)
    }

    private fun showPost(post: Post) {
        log(post)
    }

    private fun postItem(item: Item) {
        val token = requestToken()
        val post = createPost(token, item)
        showPost(post)
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        loop()

        postItem(Item("kiwi"))

        log("Hello from main")
    }
}

fun <T> background(value: T, msg: String, callback: (T) -> Unit) {
    val scheduler = Executors.newSingleThreadScheduledExecutor()
    // simulate network request
    scheduler.schedule({
        log(msg)
        callback(value)
        scheduler.shutdown()
    }, 1_000L, TimeUnit.MILLISECONDS)
}

object UsingCallback {
    private fun requestToken(callback: (Token) -> Unit) {
        background(Token(42), "Token request is being processed ...", callback)
    }

    private fun createPost(token: Token, item: Item, callback: (Post) -> Unit) {
        background(Post(token, item), "Post creation is being processed ...", callback)
    }

    private fun showPost(post: Post) {
        log(post)
    }

    private fun postItem(item: Item) {
        requestToken { token ->
            log("Token creation done")
            createPost(token, item) { post ->
                log("Post creation done")
                uiOnMain {
                    showPost(post)
                }
            }
        }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking(Dispatchers.Swing) {
        loop()

        postItem(Item("Kiwi"))

        log("Hello from main")
    }
}

object CallbackHell {
    @JvmStatic
    fun main(args: Array<String>) {
        loadData()
    }

    private fun loadData() {
        networkRequest { data ->
            anotherRequest(data) { data1 ->
                anotherRequest(data1) { data2 ->
                    anotherRequest(data2) { data3 ->
                        anotherRequest(data3) { data4 ->
                            anotherRequest(data4) { data5 ->
                                anotherRequest(data5) { data6 ->
                                    anotherRequest(data6) { data7 ->
                                        anotherRequest(data7) { data8 ->
                                            anotherRequest(data8) { data9 ->
                                                anotherRequest(data9) {
                                                    // How many more do you want?
                                                    println(it)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun networkRequest(block: (Data) -> Unit) {
        thread {
            sleep(200) // simulate network request
            block(Data(0))
        }
    }

    private fun anotherRequest(data: Data, block: (Data) -> Unit) {
        thread {
            sleep(200) // simulate network request
            block(Data(data.value + 1))
        }
    }
}

object AsyncWithCompletableFuture {

    private fun requestToken(): CompletableFuture<Token> = CompletableFuture.supplyAsync {
        log("Token request is being processed ...")
        sleep(1_000) // simulate network delay
        log("Token creation done")

        Token(42)
    }

    private fun createPost(token: Token, item: Item): CompletableFuture<Post> =
        CompletableFuture.supplyAsync {
            log("Post creation is being processed ...")
            sleep(1_000) // simulate network delay
            log("Post creation done")

            Post(token, item)
        }

    private fun showPost(post: Post) {
        log(post)
    }

    private fun postItem(item: Item) {
        requestToken()
            .thenCompose { token ->
                createPost(token, item)
            }
            .thenAccept { post ->
                uiOnMain {
                    showPost(post)
                }
            }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking(Dispatchers.Swing) {
        loop()

        postItem(Item("Kiwi"))

        log("Hello from main")
    }
}

object AsyncWithRx {

    private fun requestToken(): Observable<Token> = Observable.create { emitter ->
        log("Token request is being processed ...")
        sleep(1_000) // simulate network delay
        log("Token creation done")
        emitter.onNext(Token(42))
        emitter.onComplete()
    }

    private fun createPost(token: Token, item: Item): Observable<Post> = Observable.create { emitter ->
        log("Post creation is being processed ...")
        sleep(1_000) // simulate network delay
        log("Post creation done")

        emitter.onNext(Post(token, item))
        emitter.onComplete()
    }

    private fun showPost(post: Post) {
        log(post)
    }

    @SuppressLint("CheckResult")
    fun postItem(item: Item) {
        requestToken()
            .flatMap { token ->
                createPost(token, item)
            }
            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.from(Dispatchers.Swing.asExecutor()))
            .subscribe { post ->
                showPost(post)
            }
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking(Dispatchers.Swing) {
        loop()

        postItem(Item("Kiwi"))

        log("Hello from main")
    }
}

object AsyncWithCoroutine {

    // Suspending network request code
    private suspend fun requestToken(): Token = withContext(Dispatchers.IO) {
        log("Token request is being processed ...")
        delay(1_000) // simulate network delay
        log("Token creation done")

        Token(42)
    }

    // Suspending network request code
    private suspend fun createPost(token: Token, item: Item): Post = withContext(Dispatchers.IO) {
        log("Post creation is being processed ...")
        delay(1_000) // simulate network delay
        log("Post creation done")

        Post(token, item)
    }

    private fun showPost(post: Post) {
        log(post)
    }

    private suspend fun postItem(item: Item) {
        val token = requestToken()
        val post = createPost(token, item)
        showPost(post)
    }

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        loop()

        postItem(Item("Kiwi"))

        log("Hello from main")
    }

}