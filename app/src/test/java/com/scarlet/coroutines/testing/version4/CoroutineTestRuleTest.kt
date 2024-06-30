package com.scarlet.coroutines.testing.version4

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth
import com.scarlet.coroutines.testing.ApiService
import com.scarlet.coroutines.testing.CoroutineTestRule
import com.scarlet.model.Article
import com.scarlet.util.Resource
import com.scarlet.util.getValueForTest
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CoroutineTestRuleTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private val testArticles = Resource.Success(Article.articleSamples)

    @MockK
    private lateinit var apiService: ApiService

    // SUT
    private lateinit var viewModel: ArticleViewModel

    @Before
    fun init() {
        MockKAnnotations.init(this)

        coEvery { apiService.getArticles() } coAnswers {
            delay(3_000)
            testArticles
        }
    }

    @Test
    fun `test fun creating new coroutines`() = runTest {
        // Given
        viewModel = ArticleViewModel(apiService /* TODO */)

        // When
        viewModel.onButtonClicked()

        advanceUntilIdle()

        // Then
        coVerify { apiService.getArticles() }

        val articles = viewModel.articles.getValueForTest()
        Truth.assertThat(articles).isEqualTo(testArticles)
    }

}