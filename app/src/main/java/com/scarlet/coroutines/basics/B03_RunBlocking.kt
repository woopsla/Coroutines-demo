package com.scarlet.coroutines.basics

import com.scarlet.util.log
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

object Create_Coroutine_With_RunBlocking_Demo1 {

    @JvmStatic
    fun main(args: Array<String>) {
        log("Hello")

        runBlocking {
            log("Coroutine created")
            delay(1_000)
            log("Coroutine done")
        }

        log("World")
    }
}

object Create_Coroutine_With_RunBlocking_Demo2 {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        log("Coroutine created")

        delay(1_000)

        log("Coroutine done")
    }
}