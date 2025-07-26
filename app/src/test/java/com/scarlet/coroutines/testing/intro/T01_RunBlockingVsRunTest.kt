package com.scarlet.coroutines.testing.intro

import com.google.common.truth.Truth.assertThat
import com.scarlet.model.Article
import com.scarlet.util.log
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.system.measureTimeMillis

@ExperimentalCoroutinesApi
class RunBlockingVsRunTest {

    interface ArticleService {
        suspend fun getArticle(id: String): Article
    }

    class Repository(private val articleService: ArticleService) {
        suspend fun getArticle(id: String): Article {
            return articleService.getArticle(id)
        }
    }

    // SUT
    private lateinit var repository: Repository

    private val expectedArticle = Article("A006", "Roman Elizarov", "Kotlin Coroutines")

    @MockK
    private lateinit var mockArticleService: ArticleService

    @Before
    fun init() {
        MockKAnnotations.init(this)
        repository = Repository(mockArticleService)
    }

    @Test
    fun `runBlocking demo`() = runBlocking {
        // Given
        coEvery { // Stubbing
            mockArticleService.getArticle(any())
        } coAnswers {
            delay(2_000) // fake network delay
            expectedArticle
        }

        val duration = measureTimeMillis {
            // When
            val article = repository.getArticle("A006")
            // Then
            assertThat(article).isEqualTo(expectedArticle)
        }

        log("time elapsed = $duration")
    }

    @Test
    fun `runTest demo`() = runTest {
        // Given
        coEvery {
            mockArticleService.getArticle(any())
        } coAnswers {
            delay(2_000) // fake network delay
            expectedArticle
        }

        val duration = measureTimeMillis {
            // When
            val article = repository.getArticle("A001")
            // Then
            assertThat(article).isEqualTo(expectedArticle)
        }

        log("time elapsed = $duration")
    }
}
