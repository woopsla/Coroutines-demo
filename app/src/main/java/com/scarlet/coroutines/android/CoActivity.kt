package com.scarlet.coroutines.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
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

        Log.d(TAG, "[onCreate] massive launching started ...")

        /*
         * Use either `lifecycleScope` or `lifecycle.coroutineScope`
         */

        lifecycle.coroutineScope.launch {
            Log.d(TAG, "launch started")
            val recipes = apiService.getRecipes()
            Log.d(TAG, "recipes in launch = $recipes")
        }.invokeOnCompletion {
            Log.d(TAG, "launch completed: $it")
        }

        lifecycleScope.launchWhenCreated {
            Log.d(TAG, "launchWhenCreated started")
            val recipes = apiService.getRecipes()
            Log.d(TAG, "recipes in launchWhenCreated = $recipes")
        }.invokeOnCompletion {
            Log.d(TAG, "launchWhenCreated completed: $it")
        }

        lifecycleScope.launchWhenStarted {
            Log.d(TAG, "${spaces(2)}launchWhenStarted started")
            val recipes = apiService.getRecipes()
            Log.d(TAG, "${spaces(2)}recipes in launchWhenStarted = $recipes")
        }.invokeOnCompletion {
            Log.d(TAG, "${spaces(2)}launchWhenStarted completed: $it")
        }

        lifecycleScope.launchWhenResumed {
            Log.d(TAG, "${spaces(4)}launchWhenResumed started")
            val recipes = apiService.getRecipes()
            Log.d(TAG, "${spaces(4)}recipes in launchWhenResumed = $recipes")
        }.invokeOnCompletion {
            Log.d(TAG, "${spaces(4)}launchWhenResumed completed: $it")
        }

        lifecycleScope.launch {
            Log.d(TAG, "repeatOnLifecycle launched")
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                Log.d(TAG, "${spaces(4)}repeatOnLifeCycle at RESUMED started")
                val recipes = apiService.getRecipes()
                Log.d(TAG, "${spaces(4)}recipes in repeatOnLifeCycle = $recipes")
            }
            Log.d(TAG, "See when i am printed ...")
        }.invokeOnCompletion {
            Log.d(TAG, "launch for repeatOnLifeCycle completed: $it")
        }
    }

    private fun prepareFakeData() {
        FakeRemoteDataSource.FAKE_NETWORK_DELAY = 3_000
        apiService.addRecipes(mRecipes)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "[onStart]")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "[onStop]")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "[onPause]")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "[onResume]")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "[onDestroy]")
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

