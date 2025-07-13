package com.scarlet.coroutines.testing.intro

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * A scope for background work: `backgroundScope`.
 *
 * This scope is automatically cancelled when the test finishes.
 * A typical use case for this scope is to launch tasks that would outlive
 * the tested code in the production environment.
 */
class T06_BackgroundTest {
    @Test
    fun testExampleBackgroundJob() = runTest {
        val channel = Channel<Int>()

        // What the problem with this code? How to fix it?
        launch {
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