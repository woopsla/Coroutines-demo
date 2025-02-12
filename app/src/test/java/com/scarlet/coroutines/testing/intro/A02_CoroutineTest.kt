package com.scarlet.coroutines.testing.intro

import com.google.common.truth.Truth.assertThat
import com.scarlet.model.Article
import com.scarlet.util.log
import com.scarlet.util.testDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Test

interface ApiService {
    suspend fun populate()
    suspend fun getArticles(): List<Article>
}

class FakeApiService : ApiService {
    private val articles = mutableListOf<Article>()

    override suspend fun populate() {
        delay(1000)
        articles.add(Article("1", "Title 1", "Body 1"))
        articles.add(Article("2", "Title 2", "Body 2"))
        articles.add(Article("3", "Title 3", "Body 3"))
    }

    override suspend fun getArticles(): List<Article> {
        delay(500)
        return articles
    }
}

class Repository(
    private val apiService: ApiService,
    // TODO() - Add a coroutine dispatcher
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val scope = CoroutineScope(ioDispatcher)

    fun initialize() {
        scope.launch {
            apiService.populate();
        }
    }

    suspend fun loadData() = withContext(ioDispatcher) {
        apiService.getArticles()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class A02_CoroutineTest {
    // SUT
    private lateinit var repository: Repository

    // How to make this test pass?
    @Test
    fun repositoryTest() = runTest {
        repository = Repository(FakeApiService(), testDispatcher)
        repository.initialize()

        val articles = repository.loadData()
        assertThat(articles).containsExactly(
            Article("1", "Title 1", "Body 1"),
            Article("2", "Title 2", "Body 2"),
            Article("3", "Title 3", "Body 3")
        )
    }

}
