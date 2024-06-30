package com.scarlet.coroutines.testing.version1

import com.google.common.truth.Truth.assertThat
import com.scarlet.coroutines.testing.ApiService
import com.scarlet.model.Article
import com.scarlet.util.Resource
import com.scarlet.util.getValueForTest
import io.mockk.coEvery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ArticleViewModelTest {

    // TODO - InstantTaskExecutorRule

    // SUT
    private lateinit var viewModel: ArticleViewModel

    // TODO
    private lateinit var apiService: ApiService

    // sample test data
    private val testArticles = Resource.Success(Article.articleSamples)

    @Before
    fun init() {
        // TODO() - initialize mocks

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
        viewModel = ArticleViewModel(apiService)

        // When
        viewModel.onButtonClicked()

        delay(10_000) // Will this help?

        // Then
        val articles = viewModel.articles.getValueForTest()
        assertThat(articles).isEqualTo(testArticles)
    }
}