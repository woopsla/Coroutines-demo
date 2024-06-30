package com.scarlet.mockk

import com.google.common.truth.Truth.assertThat
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class M01_ActionHandlerTest {

    // SUT
    private lateinit var actionHandler: ActionHandler

    @MockK
    private lateinit var service: Service

    @Before
    fun init() {
        MockKAnnotations.init(this)

        actionHandler = ActionHandler(service)
    }

    @Test
    fun `should return valid string if doRequest succeed - use answers`() {
        // Given

        // When
        actionHandler.doRequest("query")

        // Then
        assertThat(actionHandler.value).isEqualTo("data")
    }

    @Test
    fun `should return null if doRequest fail - use answers`() {
        // Given

        // When
        actionHandler.doRequest("failed query")

        // Then
        assertThat(actionHandler.value).isNull()
    }

    @Test
    fun `should return valid string if doRequest succeeds - via argument captor, capture when stubbing`() {
        // Given

        // When
        actionHandler.doRequest("query")

        // Then
        assertThat(actionHandler.value).isEqualTo("data")
    }

    @Test
    fun `should return valid string if doRequest succeeds - via argument captor, capture when verify`() {
        // Given

        // When
        actionHandler.doRequest("query")

        // Then
        assertThat(actionHandler.value).isEqualTo("data")
    }
}