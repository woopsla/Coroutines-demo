package com.scarlet.coroutines.miscs

import com.scarlet.util.log
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

/**
 * `Dispatcher.Unconfined` seems to ignore `yield`.
 */
object Default_Starting_Mode {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var state = 0

        // Change the dispatcher to Dispatchers.Unconfined
        launch(context = Dispatchers.Default, start = CoroutineStart.DEFAULT) {
            state = 1
            log("child before yield: state = $state")
            yield()
            state = 2
            log("child after yield: state = $state")
            delay(1_000)
            state = 3
            log("child after 1000ms: state = $state")
        }

        log("parent before delay: state = $state")
        delay(500)
        log("parent after delay: state = $state")
    }
}

object Undispatched_Starting_Mode {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        var state = 0

        // Change the dispatcher to Dispatchers.Unconfined
        launch(context = Dispatchers.Default, start = CoroutineStart.UNDISPATCHED) {
            state = 1
            log("child before yield: state = $state")
            yield()
            state = 2
            log("child after yield: state = $state")
            delay(1_000)
            state = 3
            log("child after 1000ms: state = $state")
        }

        log("parent before delay: state = $state")
        delay(500)
        log("parent after delay: state = $state")
    }
}
