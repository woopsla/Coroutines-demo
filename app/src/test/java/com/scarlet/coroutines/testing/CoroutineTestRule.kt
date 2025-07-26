package com.scarlet.coroutines.testing

import com.scarlet.util.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class CoroutineTestRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    /*
     * testDispatchersProvider goes here
     */
    val testDispatcherProvider: DispatcherProvider = object : DispatcherProvider {
        override val main = testDispatcher
        override val mainImmediate = testDispatcher
        override val default = testDispatcher
        override val io = testDispatcher
        override val unconfined = testDispatcher
    }

    override fun starting(description: Description) {
        super.starting(description)

        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)

        Dispatchers.resetMain()
    }
}