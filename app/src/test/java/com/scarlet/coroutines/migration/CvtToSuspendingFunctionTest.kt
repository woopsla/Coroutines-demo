package com.scarlet.coroutines.migration

import com.google.common.truth.Truth.assertThat
import com.scarlet.model.Recipe
import com.scarlet.model.Recipe.Companion.mRecipes
import com.scarlet.util.Resource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executors.*
import java.util.concurrent.TimeUnit

class CvtToSuspendingFunctionTest {

    @MockK
    lateinit var mockApi: RecipeApi

    @MockK(relaxed = true)
    lateinit var mockCall: Call<List<Recipe>>

    @MockK
    lateinit var mockResponse: Response<List<Recipe>>

    @Before
    fun init() {
        MockKAnnotations.init(this)
        every { mockApi.search(any(), any()) } returns mockCall
    }

    @Test
    fun `Callback - should return valid recipes`() {
        // Arrange (Given)
        val slot = slot<Callback<List<Recipe>>>()
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns mRecipes
        every { mockCall.enqueue(capture(slot)) } answers {
            slot.captured.onResponse(mockCall, mockResponse)
        }

        val target = UsingCallback_Demo2

        // Act (When)
        target.searchRecipes("eggs", mockApi, object : RecipeCallback<List<Recipe>> {
            override fun onSuccess(response: Resource<List<Recipe>>) {
                // Assert (Then)
                assertThat(response).isEqualTo(Resource.Success(mRecipes))
            }

            override fun onError(response: Resource<List<Recipe>>) {
                fail("Should not be called")
            }
        })
    }

//    @ExperimentalCoroutinesApi
//    @Test
//    fun `Suspending Function - should return valid recipes`() = runTest {
//        // Arrange (Given)
//        val slot = slot<Callback<List<Recipe>>>()
//        every { mockResponse.isSuccessful } returns true
//        every { mockResponse.body() } returns mRecipes
//        every { mockCall.enqueue(capture(slot)) } answers {
//            slot.captured.onResponse(mockCall, mockResponse)
//        }
//
//        val target = CvtToSuspendingFunction_Demo2
//
//        // Act (When)
//        val response = target.searchRecipes("eggs", mockApi, TODO())
//
//        // Assert (Then)
//        assertThat(response).isEqualTo(Resource.Success(mRecipes))
//    }

    @Test
    fun `Suspending Function - should return valid recipes`() = runTest {
        // Arrange (Given)
        val slot = slot<Callback<List<Recipe>>>()
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns mRecipes
        every { mockCall.enqueue(capture(slot)) } answers {
            slot.captured.onResponse(mockCall, mockResponse)
        }

        val target = CvtToSuspendingFunction_Demo2

        // Act (When)
        val response = target.searchRecipes("eggs", mockApi, TODO())

        // Assert (Then)
        assertThat(response).isEqualTo(Resource.Success(mRecipes))
    }

    @Test
    fun `Suspending Function - should cancel searchRecipes`() = runBlocking {
        // Arrange (Given)
        val slot = slot<Callback<List<Recipe>>>()
        every { mockResponse.isSuccessful } returns true
        every { mockResponse.body() } returns mRecipes
        every {
            mockCall.enqueue(capture(slot))
        } answers {
            newSingleThreadScheduledExecutor().schedule({
                slot.captured.onResponse(mockCall, mockResponse)
            }, 1, TimeUnit.SECONDS)
        }

        val target = CvtToSuspendingFunction_Demo2

        // Act (When)
        val job = launch {
            target.searchRecipes("eggs", mockApi, TODO())
        }

        delay(500)
        job.cancelAndJoin()

        // Assert (Then)
        verify { mockCall.cancel() }
    }
}