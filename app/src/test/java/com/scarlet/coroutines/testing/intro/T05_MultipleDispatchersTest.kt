package com.scarlet.coroutines.testing.intro

import com.google.common.truth.Truth.assertThat
import com.scarlet.util.log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class T05_MultipleDispatchersTest {
    /**
     * Note by Kim --> Somewhat mismatch with formal documentation ㅠㅠ
     * `StandardTestDispatcher` and `UnconfinedTestDispatcher`.
     * 1. Every `StandardTestDispatcher` will create different `TestCoroutineDispatcher`.
     * 2. Every `UnconfinedTestDispatcher` will use already existing `TestCoroutineDispatcher`
     *    regardless of the types of TestCoroutineDispatcher.
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
    fun `same test scheduler is used2`() = runTest(UnconfinedTestDispatcher()) {

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

    fun `make same test scheduler be used`() = runTest {

        launch(StandardTestDispatcher(testScheduler)) {
        }
    }

    @Test
    fun `somewhat surprise test`() = runTest {

        log("0")

        launch {
            log("1")
        }

        // ???
        launch(UnconfinedTestDispatcher(TestCoroutineScheduler())) {
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