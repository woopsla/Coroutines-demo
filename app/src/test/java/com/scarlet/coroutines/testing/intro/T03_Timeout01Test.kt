package com.scarlet.coroutines.testing.intro

import com.google.common.truth.Truth.assertThat
import com.scarlet.model.User
import com.scarlet.util.log
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

interface UserService {
    suspend fun load(): User
}

suspend fun loadUser(userService: UserService): User =
    withTimeout(5_000) {
        userService.load()
    }

@ExperimentalCoroutinesApi
class Timeout01Test {

    @MockK
    lateinit var userService: UserService

    private val testUser = User("101", "Peter Parker", 25)

    @Before
    fun init() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `load responds immediately`() = runTest {
        coEvery { userService.load() } returns testUser

        val user = loadUser(userService)

        log("$currentTime")

        assertThat(user).isEqualTo(testUser)
    }

    @Test
    fun `load in less than 5 seconds succeeds`() = runTest {
        coEvery { userService.load() } coAnswers {
            delay(4_999)
            testUser
        }

        val user = loadUser(userService)
        assertThat(user).isEqualTo(testUser)
        log("$currentTime")
    }

    @Test(expected = TimeoutCancellationException::class)
    fun `load timed out after 5 seconds`() = runTest {
        coEvery { userService.load() } coAnswers {
            delay(5_000)
            testUser
        }

        loadUser(userService)
    }

    /**
     * Testing `async` case
     */

    private fun CoroutineScope.loadUserAsync(userService: UserService): Deferred<User> = async {
        withTimeout(5_000) {
            userService.load()
        }
    }

    @Test
    fun `testing async in time`() = runTest {
        coEvery { userService.load() } coAnswers {
            delay(4_999)
            testUser
        }

        val deferred = loadUserAsync(userService)

        val user = deferred.await()
        log("$currentTime")

        assertThat(user).isEqualTo(testUser)
    }

    @Test(expected = TimeoutCancellationException::class)
    fun `testing async timeout`() = runTest {
        coEvery { userService.load() } coAnswers {
            delay(5_000)
            testUser
        }

        val deferred = loadUserAsync(userService)

        deferred.await()
    }

}