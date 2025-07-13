package com.scarlet.coroutines.testing.intro

import com.google.common.truth.Truth.assertThat
import com.scarlet.util.log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@ExperimentalCoroutinesApi
class T05_MultipleDispatchersTest {
    /**
     * Use of different schedulers in a test causes `IllegalStateException`.
     * If you need to use several test coroutine dispatchers, create one `TestCoroutineScheduler` and
     * pass it to each of them.
     *
     * Note by Kim --> Somewhat mismatch with formal documentation ㅠㅠ
     * `StandardTestDispatcher` vs. `UnconfinedTestDispatcher`.
     * 1. Every `StandardTestDispatcher` will use different `TestCoroutineScheduler`.
     * 2. Every `UnconfinedTestDispatcher` will use already existing `TestCoroutineScheduler`.
     * 3. Every `TestCoroutineDispatcher` will reuse the same `TestCoroutineScheduler`
     *    used by mocked `TestCoroutineDispatcher` with `Dispatchers.setMain(testDispatcher)`.
     */
    @Test
    fun `same test scheduler is used1`() = runTest {

        launch(UnconfinedTestDispatcher()) {
        }

        launch(UnconfinedTestDispatcher()) {
        }
    }

    @Test
    fun `same test scheduler is used2`() =
        runTest(UnconfinedTestDispatcher()) {

            launch(UnconfinedTestDispatcher()) {
            }

            launch(UnconfinedTestDispatcher()) {
            }
        }

    @Test(expected = IllegalStateException::class)
    fun `different test scheduler is used1`() = runTest {

        launch(StandardTestDispatcher()) {
        }
    }


    @Test(expected = IllegalStateException::class)
    fun `different test scheduler is used2`() = runTest(UnconfinedTestDispatcher()) {

        launch(StandardTestDispatcher()) {
        }
    }

    @Test
    fun `make same test scheduler be used`() = runTest {

        launch(StandardTestDispatcher(testScheduler)) {
        }
    }

    @Test
    fun `somewhat intriguing test`() = runTest {

        log("0")

        launch {
            log("1")
        }

        launch(UnconfinedTestDispatcher(testScheduler)) {
            log("2")
            launch {
                log("3")
            }
        }

        log("4")
    }

    @Test
    fun testEagerlyEnteringSomeChildCoroutines() = runTest(UnconfinedTestDispatcher()) {
        var entered1 = false
        launch {
            entered1 = true
        }
        assertThat(entered1).isTrue() // `entered1 = true` already executed

        var entered2 = false
        launch(StandardTestDispatcher(testScheduler)) {
            // this block and every coroutine launched inside it will explicitly go through the needed dispatches
            entered2 = true
        }
        assertThat(entered2).isFalse()
        runCurrent() // need to explicitly run the dispatched continuation
        assertThat(entered2).isTrue()
    }
}