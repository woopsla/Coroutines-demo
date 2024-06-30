package com.scarlet.coroutines.advanced

import com.scarlet.util.log
import java.util.concurrent.Executors

object SuspendOrigin {
    private fun fooWithDelay(a: Int, b: Int): Int {
        log("step 1")
        Thread.sleep(3_000)
        log("step 2")
        return a + b
    }

    @JvmStatic
    fun main(args: Array<String>) {
        log("main started")

        log("result = ${fooWithDelay(3, 4)}")

        log("main end")
    }

    private val executor = Executors.newSingleThreadScheduledExecutor {
        Thread(it, "scheduler").apply { isDaemon = true }
    }
}

