package com.scarlet.coroutines.testing

import androidx.lifecycle.LiveData
import com.scarlet.model.Article
import com.scarlet.util.Resource

interface ApiService {
    /**
     * Get all articles
     */
    suspend fun getArticles(): Resource<List<Article>>

    /**
     * Get the most recommended (i.e., top-ranked) article
     */
    suspend fun getTopArticle(): Resource<Article>

    /**
     * Get all the articles written by a specific author
     */
    fun getArticlesByAuthorName(name: String): LiveData<Resource<List<Article>>>
}