package com.scarlet.coroutines.testing.intro

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class T06_BackgroundTest {
    @Test
    fun testExampleBackgroundJob() = runTest {
        val channel = Channel<Int>()
        backgroundScope.launch {
            var i = 0
            while (true) {
                channel.send(i++)
            }
        }
        repeat(100) {
            assertThat(channel.receive()).isEqualTo(it)
        }
    }
}