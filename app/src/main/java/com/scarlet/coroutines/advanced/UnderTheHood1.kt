package com.scarlet.coroutines.advanced

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun foo(): Int {
    // label 0
    delay(1_000)
    //label 1
    return 42
}

//suspend fun bar(n: Int): List<Int> {
//    // label 0
//    val list = mutableListOf(1, 2, 3)
//    delay(1_000)
//    // label 1
//    list.add(n)
//    return list
//}

@DelicateCoroutinesApi
fun main() {
    GlobalScope.launch {
        val result = foo()
        println(result)
    }
    Thread.sleep(2_000)
}