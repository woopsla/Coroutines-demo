package com.scarlet.coroutines.advanced

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

suspend fun foo(arg: Int): Double {
    // label 0
    var local1 = 1.0
    var local2 = 2.0
    val res1 = bar(arg, local1)
    //label 1
    val res2 = bar(arg + 1, local2 + 1.0)
    //label 2
    return res1 + res2
}

suspend fun bar(m: Int, n: Double): Double {
    // label 0
    delay(1_000)
    // label 1
    return m + n
}

@DelicateCoroutinesApi
fun main() {
    GlobalScope.launch {
        val result = foo(1)
        println(result)
    }
    Thread.sleep(2_000)
}