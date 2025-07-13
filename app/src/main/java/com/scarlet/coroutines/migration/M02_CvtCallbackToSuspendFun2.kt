package com.scarlet.coroutines.migration

import com.scarlet.model.Recipe
import com.scarlet.util.Resource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

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
    /*
     * TODO: Convert this method to a suspending function
     */
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