package com.scarlet.coroutines.testing.exercises

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

interface UserRepo {
    suspend fun register(user: String)
    suspend fun getAllUsers(): List<String>
}

class FakeUserRepo : UserRepo {
    private val users = mutableListOf<String>()

    override suspend fun register(user: String) {
        users.add(user)
    }

    override suspend fun getAllUsers(): List<String> {
        return users
    }
}

class A01_CoroutineTest {
    // SUT
    lateinit var userRepo: UserRepo

    @Before
    fun setUp() {
        userRepo = FakeUserRepo()
    }

    // How to make this test pass?
    @Test
    fun `should register user`() = runTest(UnconfinedTestDispatcher()) {
        // Given
        // When
        launch {
            userRepo.register("Lindsay Wagner")
        }
        launch {
            userRepo.register("Diane Lane")
        }

        // Then
        assertThat(userRepo.getAllUsers()).containsExactly("Lindsay Wagner", "Diane Lane")
    }

    @Test
    fun `should register user2`() = runTest {
        // Given
        // When
        launch(UnconfinedTestDispatcher()) {
            userRepo.register("Lindsay Wagner")
        }
        launch(UnconfinedTestDispatcher()) {
            userRepo.register("Diane Lane")
        }

        // Then
        assertThat(userRepo.getAllUsers()).containsExactly("Lindsay Wagner", "Diane Lane")
    }

    @Test
    fun `should register user3`() = runTest {
        // Given
        // When
        coroutineScope {
            launch {
                userRepo.register("Lindsay Wagner")
            }
            launch {
                userRepo.register("Diane Lane")
            }
        }

        // Then
        assertThat(userRepo.getAllUsers()).containsExactly("Lindsay Wagner", "Diane Lane")
    }
}