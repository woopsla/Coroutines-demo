package com.scarlet.coroutines.testing.intro

import com.google.common.truth.Truth.assertThat
import com.scarlet.util.log
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Test

@ExperimentalCoroutinesApi
class VirtualTimeControlTest {

    @Test
    fun `virtual time control - StandardTestDispatcher`() = runTest {
        var state = 0

        launch {
            state = 1
            delay(1_000)
            state = 2
        }

        // What `state` value should we compare with to make the test pass? 0 or 1?
//        assertThat(state).isEqualTo(TODO())
        assertThat(state).isEqualTo(0)
        log("$currentTime")
    }

    @Test
    fun `virtual time control - UnconfinedCoroutineDispatcher - eager`() =
        runTest(UnconfinedTestDispatcher()) {
            var state = 0

            launch {
                state = 1
                delay(1_000)
                state = 2
            }

            // What `state` value should we compare with to make the test pass? 0 or 1?
            assertThat(state).isEqualTo(1)
            log("$currentTime")
        }

    @Test
    fun `test virtual time control - StandardTestDispatcher`() = runTest {
        var count = 0

        launch {
            log("child start")
            delay(1_000)
            count = 1
            delay(1_000)
            count = 3
            delay(1_000)
            count = 5
            log("child end")
        }

        assertThat(count).isEqualTo(0)
        log("$currentTime")

        advanceTimeBy(1_000);
        log("$currentTime"); runCurrent()
        assertThat(count).isEqualTo(1)

        advanceTimeBy(1_000); runCurrent()
        log("$currentTime")
        assertThat(count).isEqualTo(3)

//        advanceTimeBy(1_000); runCurrent()
        advanceUntilIdle()
        log("$currentTime")
        assertThat(count).isEqualTo(5)
    }

    @Test
    fun `runCurrent & advanceUntilIdle demo`() = runTest(UnconfinedTestDispatcher()) {
        var state = 0

        launch {
            state = 1
            delay(1_000)
            state = 2
            delay(1_000)
            state = 3
            delay(1_000)
            state = 4
        }

        assertThat(state).isEqualTo(1)
        log("$currentTime")

//        // `runCurrent` run any tasks that are pending at or before the current virtual clock-time.
//        // Calling this function will never advance the clock.
        advanceTimeBy(1_000); runCurrent()
        assertThat(state).isEqualTo(2)
        log("$currentTime")
//
//        // Immediately execute all pending tasks and advance the virtual clock-time to the last delay.
//        // If new tasks are scheduled due to advancing virtual time, they will be executed before
//        // `advanceUntilIdle` returns.
        advanceUntilIdle()
        assertThat(state).isEqualTo(4)
        log("$currentTime")
    }

    @Test
    fun `paused and resume dispatcher - realistic example`() = runTest {

        val list = mutableListOf<Int>().apply {
            add(42)
            launch {
                log(Thread.currentThread().name)
                add(777)
            }
        }

        assertThat(list).containsExactly(42)

        // How to make the test pass?
//        TODO()
//        delay(1)
//        runCurrent()
//        advanceTimeBy(1)
        advanceUntilIdle()

        assertThat(list).containsExactly(42, 777)
    }
}

/**
 * Deprecated Example
 */
//    @Test
//    fun `paused and resume dispatcher - realistic example - runBlockingTest`() = runBlockingTest {
//
//        pauseDispatcher()
//        val list = mutableListOf<Int>().apply {
//            add(42)
//            launch {
//                log(Thread.currentThread().name)
//                add(777)
//            }
//        }
//
//        assertThat(list).containsExactly(42)
//
//        resumeDispatcher()
//
//        assertThat(list).containsExactly(42, 777)
//    }