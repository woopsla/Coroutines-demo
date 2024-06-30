package com.scarlet.coroutines.android

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.scarlet.R
import kotlinx.coroutines.*

class ScopedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val handler = CoroutineExceptionHandler { _, exception ->
            Log.e(TAG, "CoroutineExceptionHandler got $exception")
        }

        lifecycleScope.launch {
            Log.i(TAG, "parent started")

            supervisorScope {
                coroutineContext.job.invokeOnCompletion { ex ->
                    Log.e(
                        TAG,
                        "supervisorScope: isCancelled = ${coroutineContext.job.isCancelled}, cause = $ex"
                    )
                }
                launch(handler) {
                    Log.i(TAG, "child 1 started")
                    delay(2_000)
                    throw RuntimeException("OOPS!")
                }.apply {
                    invokeOnCompletion {
                        Log.i(TAG, "Child 1: isCancelled = $isCancelled, cause = $it")
                    }
                }

                launch {
                    Log.i(TAG, "child 2 started")
                    delay(5_000)
                }.apply {
                    invokeOnCompletion {
                        Log.i(TAG, "Child2: isCancelled = $isCancelled, cause = $it")
                    }
                }

                Log.i(TAG, "inside subScope... ")
            }

        }.apply {
            invokeOnCompletion {
                Log.i(TAG, "Parent: isCancelled = $isCancelled, cause = $it")
            }
        }
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
        const val TAG = "Scoped"
    }
}

