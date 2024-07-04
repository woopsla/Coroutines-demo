package com.scarlet.coroutines.advanced

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Token(val key: Int)
data class Item(val item: String)
data class Post(val content: String)

suspend fun postItem(item: Item) {
    val token = requestToken()
    val post = createPost(token, item)
    showPost(post)
}

suspend fun requestToken(): Token {
    delay(100)
    return Token(1)
}

suspend fun createPost(token: Token, item: Item): Post {
    delay(1000)
    return Post("Post created with ${item.item} and token ${token.key}")
}

fun showPost(post: Post) {
    println(post)
}

@DelicateCoroutinesApi
fun main() {
    GlobalScope.launch {
        postItem(Item("Hello World"))
    }
}