package com.scarlet.coroutines.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.scarlet.R
import com.scarlet.model.Recipe
import com.scarlet.model.Recipe.Companion.mRecipes
import com.scarlet.util.Resource
import com.scarlet.util.spaces
import kotlinx.coroutines.*
import java.util.LinkedHashMap

@ExperimentalCoroutinesApi
class CoActivity : AppCompatActivity() {
    private val apiService = FakeRemoteDataSource()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prepareFakeData()

        /*
         * Use either `lifecycleScope` or `lifecycle.coroutineScope`
         */

        Log.e(TAG, "[onCreate] launching started ...")
        lifecycle.coroutineScope.launch {
            Log.e(TAG, "launch started")
            val recipes = apiService.getRecipes()
            Log.e(TAG, "recipes in launch = $recipes")
        }.invokeOnCompletion {
            Log.e(TAG, "launch completed: $it")
        }

        /*
         * Deprecated methods:
         * - lifecycleScope.{launchWhenCreated, launchWhenStarted, launchWhenResumed}
         *
         * Use `lifecycle.repeatOnLifecycle` instead.
         */

//        lifecycleScope.launch {
//            Log.e(TAG, "repeatOnLifecycle launched, job = ${coroutineContext[Job]}")
//            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
//                Log.e(
//                    TAG,
//                    "${spaces(4)}repeatOnLifeCycle at RESUMED started, job = ${coroutineContext[Job]}"
//                )
//                val recipes = apiService.getRecipes()
//                Log.e(TAG, "${spaces(4)}recipes in repeatOnLifeCycle = $recipes")
//            }
//            Log.e(TAG, "See when i am printed ...")
//        }.invokeOnCompletion {
//            Log.e(TAG, "launch for repeatOnLifeCycle completed: $it")
//        }
    }

    private fun prepareFakeData() {
        FakeRemoteDataSource.FAKE_NETWORK_DELAY = 1_000
        apiService.addRecipes(mRecipes)
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "[onStart]")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "[onStop]")
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "[onPause]")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "[onResume]")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "[onDestroy]")
    }

    companion object {
        const val TAG = "Coroutine"
    }

    private class FakeRemoteDataSource {
        private val mRecipes: MutableMap<String, Recipe> = LinkedHashMap<String, Recipe>()

        suspend fun getRecipes(): Resource<List<Recipe>> {
            return withContext(Dispatchers.IO) {
                delay(FAKE_NETWORK_DELAY)
                Resource.Success(mRecipes.values.toList())
            }
        }

        fun addRecipes(recipes: List<Recipe>) {
            recipes.forEach { recipe -> mRecipes[recipe.recipeId] = recipe.copy() }
        }

        companion object {
            var FAKE_NETWORK_DELAY = 0L
        }
    }
}

