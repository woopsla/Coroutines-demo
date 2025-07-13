package com.scarlet.coroutines.testing.version2

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.scarlet.coroutines.testing.ApiService
import com.scarlet.model.Article
import com.scarlet.util.Resource
import com.scarlet.util.getValueForTest
import com.scarlet.util.log
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

/**
 * TODO: Use `Dispatchers.setMain(`) to replace `Dispatchers.Main`.
 */

@ExperimentalCoroutinesApi
class SetMainTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val testArticles = Resource.Success(Article.articleSamples)

    @MockK
    private lateinit var mockApiService: ApiService

    // SUT
    private lateinit var viewModel: ArticleViewModel

    // TODO: Create a test dispatcher

    @Before
    fun init() {
        MockKAnnotations.init(this)

        coEvery { mockApiService.getArticles() } coAnswers {
            log("coAnswers")
            delay(3_000)
            testArticles
        }
    }

    @Test
    fun `test fun creating new coroutines`() = runTest {
        // Given
        viewModel = ArticleViewModel(mockApiService)

        // When
        viewModel.onButtonClicked()

        advanceUntilIdle()

        // Then
        coVerify { mockApiService.getArticles() }

        val articles = viewModel.articles.getValueForTest()
        assertThat(articles).isEqualTo(testArticles)
    }
}
