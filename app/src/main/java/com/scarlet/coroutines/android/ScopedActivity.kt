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

                // TODO: Install the custom handler here ...
                launch {
                    Log.e(TAG, "child 1 started")
                    delay(2_000)
                    Log.e(TAG, "child 1: I'm about to throwing exception")
                    throw RuntimeException("OOPS!")
                }.apply {
                    invokeOnCompletion {
                        Log.e(TAG, "Child 1: isCancelled = $isCancelled, cause = $it")
                    }
                }

                launch {
                    Log.e(TAG, "child 2 started")
                    delay(5_000)
                }.apply {
                    invokeOnCompletion {
                        Log.e(TAG, "Child 2: isCancelled = $isCancelled, cause = $it")
                    }
                }
            }

        }.apply {
            invokeOnCompletion {
                Log.e(TAG, "Parent: isCancelled = $isCancelled, cause = $it")
            }
        }
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
        const val TAG = "Scoped"
    }
}

