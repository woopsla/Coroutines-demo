package com.scarlet.coroutines.testing.intro

import com.google.common.truth.Truth.assertThat
import com.scarlet.util.log
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters

class Subject {
    var someBoolean = false

    fun CoroutineScope.loop() {
        someBoolean = true

        launch {
            repeat(10) { count ->
                delay(1_000)
                log("loop is running -- $count")
            }
            log("all done")
        }
    }
}

@ExperimentalCoroutinesApi
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class CoroutineLeakTest {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val subject = Subject()

    @Before
    fun before() {
        log("before my leaky test")
    }

    @After
    fun after() {
//        scope.cancel()  // One way to solve leaking problem
        log("after my leaky test")
    }

    @Test
    fun `create a leak`() = runTest {

        with(subject) {
            scope.loop()
        }

        log("my leaky test has completed")
        assertThat(subject.someBoolean).isTrue()
    }

    @Test
    fun `create another test`() {

        log("some other tests would run now")

        runBlocking { delay(5_000) } //  This mimics execution of other tests while a leak is happening.
    }

}