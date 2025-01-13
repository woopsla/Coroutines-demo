package com.scarlet.coroutines.testing.version3

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.scarlet.coroutines.testing.ApiService
import com.scarlet.model.Article
import com.scarlet.util.Resource
import com.scarlet.util.getValueForTest
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CoroutineTestRuleTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    // TODO: Add a test for the coroutineTestRule

    private val testArticles = Resource.Success(Article.articleSamples)

    @MockK
    private lateinit var apiService: ApiService

    // SUT
    private lateinit var viewModel: ArticleViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun init() {
        MockKAnnotations.init(this)

        coEvery { apiService.getArticles() } coAnswers {
            delay(3_000)
            testArticles
        }

        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test fun creating new coroutines`() = runTest {
        // Given
        viewModel = ArticleViewModel(apiService)

        // When
        viewModel.onButtonClicked()

        var articles = viewModel.articles.getValueForTest()
        assertThat(articles).isEqualTo(Resource.Loading)

        advanceUntilIdle()

        // Then
        coVerify { apiService.getArticles() }

        articles = viewModel.articles.getValueForTest()
        assertThat(articles).isEqualTo(testArticles)
    }
}
