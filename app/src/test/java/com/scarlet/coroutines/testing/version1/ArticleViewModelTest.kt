package com.scarlet.coroutines.testing.version1

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.scarlet.coroutines.testing.ApiService
import com.scarlet.model.Article
import com.scarlet.util.Resource
import com.scarlet.util.getValueForTest
import com.scarlet.util.testDispatcher
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * TODO: Inject a test dispatcher to ViewModel.
 */

@ExperimentalCoroutinesApi
class ArticleViewModelTest {

    // TODO - InstantTaskExecutorRule
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // SUT
    private lateinit var viewModel: ArticleViewModel

    // TODO
    @MockK
    private lateinit var apiService: ApiService

    // sample test data
    private val testArticles = Resource.Success(Article.articleSamples)

    @Before
    fun init() {
        // TODO() - initialize mocks
        MockKAnnotations.init(this)

        coEvery { apiService.getArticles() } coAnswers {
            delay(3_000)
            testArticles
        }
    }

    // More on livedata testing later ...
    @Test
    fun `loadData - test suspend fun not creating new coroutines`() = runTest {
        // Given
        viewModel = ArticleViewModel(apiService)

        // When
        viewModel.loadData()

        // Then
        val articles = viewModel.articles.getValueForTest()
        assertThat(articles).isEqualTo(testArticles)
    }

    @Test
    fun `onButtonClicked - test fun creating new coroutines - runBlocking`() = runBlocking {
        // Given
        viewModel = ArticleViewModel(apiService)

        // When
        viewModel.onButtonClicked()

        delay(3_000)

        // Then
        val articles = viewModel.articles.getValueForTest()
        assertThat(articles).isEqualTo(testArticles)
    }

    @Test
    fun `onButtonClicked - test fun creating new coroutines - runTest`() = runTest {
        // Given
        viewModel = ArticleViewModel(apiService, testDispatcher)

        // When
        viewModel.onButtonClicked()

        advanceUntilIdle() // Will this help?

        // Then
        val articles = viewModel.articles.getValueForTest()
        assertThat(articles).isEqualTo(testArticles)
    }
}