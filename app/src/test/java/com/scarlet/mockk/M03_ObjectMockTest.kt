package com.scarlet.mockk

import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Test

object RemoteDataSource {
    val fruits = mutableListOf("Apple", "Banana", "Orange")

    fun searchFruits(query: String): List<String> =
        fruits.filter { it.contains(query, ignoreCase = true) }

    fun addFruit(fruit: String) {
        fruits.add(fruit)
    }
}

class ObjectMockTest {

    @Test
    fun `object mock test`() {
        // Arrange (Given)
        println(RemoteDataSource.fruits)

        mockkObject(RemoteDataSource)

        every { RemoteDataSource.searchFruits(any()) } returns listOf("Pineapple")
        every { RemoteDataSource.addFruit(any()) } just Runs

        // Act (When)
        val result = RemoteDataSource.searchFruits("ea")
        RemoteDataSource.addFruit("Kiwi")

        // Assert (Then)
        assertThat(result).containsExactly("Pineapple")

        // reset object
        unmockkObject(RemoteDataSource) // or use unmockkAll()

        // check the original state
        println(RemoteDataSource.fruits)
    }

}
