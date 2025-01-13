package com.scarlet.coroutines.basics

import com.scarlet.model.User
import com.scarlet.util.log
import kotlinx.coroutines.*
import kotlinx.coroutines.delay

private suspend fun save(user: User) {
    delay(1_000) // simulate some delay
    log("User saved: $user")
}

object Launch_Demo1 {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        log("1. before launch")

        launch {
            log("3. before save")
            save(User("A001", "Jody", 33))
            log("4. after save")
        }

        log("2. after launch")
    }

}

object Launch_Demo2 {

    @JvmStatic
    fun main(args: Array<String>) {
        log("X. Start")

        runBlocking {
            launch {
                delay(1_000)
                log("X. child 1 done.")
            }
            launch {
                delay(2_000)
                log("X. child 2 done.")
            }

            log("X. end of runBlocking")
        }

        log("X. Done")
    }
}

object Launch_Join_Demo {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        log("1. start of runBlocking")

        launch {
            log("2. child 1 start")
            delay(1_000)
            log("3. child 1 done")
        }

        // How to print next line at the last?
        log("4. Done")
    }
}

// DON'T DO THIS
@DelicateCoroutinesApi
object GlobalScope_Demo {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        log("1. start of runBlocking")

        GlobalScope.launch {
            log("2. before save")
            save(User("A001", "Jody", 33))
            log("3. after save")
        }.join()

        log("4. Done.")
    }

}

/**
 * See what happens when you use w/ or w/o `runBlocking`, and then when use `join`.
 */
object CoroutineScope_Sneak_Preview_Demo {

    @JvmStatic
    fun main(args: Array<String>) {
        // If the given context does not contain a Job element,
        // then a default Job() is created.
        val scope = CoroutineScope(Job())

        val job = scope.launch {
            log("1. before save")
            save(User("A001", "Jody", 33))
            log("2. after save")
        }

        // force the main thread wait
//        Thread.sleep(2000)
//        runBlocking { job.join() }

        log("3. Done.")
    }
}


