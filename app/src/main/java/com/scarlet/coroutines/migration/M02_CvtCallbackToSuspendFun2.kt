package com.scarlet.coroutines.migration

import com.scarlet.model.Recipe
import com.scarlet.util.Resource
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import kotlin.coroutines.resume

interface RecipeApi {
    @GET("api/search")
    fun search(
        @Query("key") key: String,
        @Query("q") query: String
    ): Call<List<Recipe>>
}

interface RecipeCallback<T> {
    fun onSuccess(response: Resource<T>)
    fun onError(response: Resource<T>)
}

object UsingCallback_Demo2 {

    fun searchRecipes(
        query: String, api: RecipeApi, callback: RecipeCallback<List<Recipe>>
    ) {
        val call = api.search("key", query)
        call.enqueue(object : Callback<List<Recipe>> {
            override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                if (response.isSuccessful) {
                    callback.onSuccess(Resource.Success(response.body()!!))
                } else {
                    callback.onError(Resource.Error(response.message()))
                }
            }

            override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                callback.onError(Resource.Error(t.message))
            }
        })
    }
}

object CvtToSuspendingFunction_Demo2 {

    fun searchRecipes(
        query: String, api: RecipeApi, callback: RecipeCallback<List<Recipe>>
    ) {
        val call = api.search("key", query)
        call.enqueue(object : Callback<List<Recipe>> {
            override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                if (response.isSuccessful) {
                    callback.onSuccess(Resource.Success(response.body()!!))
                } else {
                    callback.onError(Resource.Error(response.message()))
                }
            }

            override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                callback.onError(Resource.Error(t.message))
            }
        })
    }

    // Use Call.await()
    suspend fun searchRecipesV2(query: String, api: RecipeApi): Resource<List<Recipe>> {
        val call: Call<List<Recipe>> = api.search("key", query)

        TODO()
    }
}

suspend fun <T> Call<T>.await(): Resource<T> = suspendCancellableCoroutine { continuation ->
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            if (response.isSuccessful) {
                continuation.resume(Resource.Success(response.body()!!))
            } else {
                continuation.resume(Resource.Error(response.message()))
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            continuation.resume(Resource.Error(t.message))
        }
    })

    continuation.invokeOnCancellation {
        cancel()
    }
}