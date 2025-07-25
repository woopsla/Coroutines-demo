package com.scarlet.coroutines.migration

import com.scarlet.util.log
import kotlinx.coroutines.runBlocking
import java.io.IOException

// Callback
private interface AsyncCallback {
    fun onSuccess(result: String)
    fun onError(ex: Exception)
}

object UsingCallback_Demo1 {

    // Method using callback to simulate a long running task
    private fun getData(callback: AsyncCallback, status: Boolean = true) {
        // Do network request here, and then respond accordingly
        if (status) {
            callback.onSuccess("Congratulations!")
        } else {
            callback.onError(IOException("Network failure"))
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val callback = object : AsyncCallback {
            override fun onSuccess(result: String) {
                log("Data received: $result")
            }

            override fun onError(ex: Exception) {
                log("Caught ${ex.javaClass.simpleName}")
            }
        }

        getData(callback, true) // for success case
        getData(callback, false) // for error case
    }
}

object CvtToSuspendingFunction_Demo1 {
    /*
     * Use `resume/resumeWithException` or `resumeWith` only
     */
    private suspend fun getData(status: Boolean = true): String = TODO()

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {
        method1() // for success case
        method1(false) // for error case

        method2() // for success case
        method2(false) // for error case
    }

    suspend fun method1(status: Boolean = true) {
        try {
            getData(status).also {
                log("Data received: $it")
            }
        } catch (ex: Exception) {
            log("Caught ${ex.javaClass.simpleName}")
        }
    }

    suspend fun method2(status: Boolean = true) {
        runCatching { getData(status) }
            .onSuccess { log("Data received: $it") }
            .onFailure {
                log("Caught ${it.javaClass.simpleName}")
            }
    }
}