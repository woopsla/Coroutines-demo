package com.scarlet.coroutines.testing.intro

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test

interface Api {
    suspend fun fetch(): String
}

private class SuspendingFakeApi : Api {
    val deferred = CompletableDeferred<String>()

    override suspend fun fetch(): String {
        return deferred.await() // wait forever ...
    }
}

suspend fun loadData(api: Api): String = withTimeout(5_000) {
    api.fetch()
}

@ExperimentalCoroutinesApi
class Timeout02Test {

    @Test(expected = TimeoutCancellationException::class)
    fun `Wrong test`() = runTest {

        val api = SuspendingFakeApi()

        log("result = ${loadData(api)}") // already timeout ...
    }

    @Test
    fun `check timeout cancellation demo`() = runTest {
        val api = SuspendingFakeApi()

        launch {
            log("result = ${loadData(api)}")
        }.onCompletion("job")

        advanceTimeBy(4_999) // 4999 ~ 5000 ms
        api.deferred.complete("Hello")

        log("Done.")
    }
}