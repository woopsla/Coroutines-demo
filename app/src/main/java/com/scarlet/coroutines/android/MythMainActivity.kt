package com.scarlet.coroutines.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.scarlet.R
import kotlinx.coroutines.*
import java.math.BigInteger
import java.util.*
import kotlin.system.exitProcess

class MythMainActivity : AppCompatActivity() {

    private lateinit var textView: TextView
    private lateinit var findButton: Button
    private lateinit var cancelButton: Button
    private lateinit var status: TextView

    private var primeJob: Job? = null
    private var countingJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_myth)

        textView = findViewById(R.id.counter)
        findButton = findViewById(R.id.startButton)
        cancelButton = findViewById<Button>(R.id.stopButton).apply {
            isEnabled = false
        }

        status = findViewById(R.id.findBigPrime)

        findButton.setOnClickListener {
            findButton.isEnabled = false
            cancelButton.isEnabled = true
            status.text = "Calculating big prime number ..."

            showSnackbar("Launching findBigPrime ...")

            primeJob = lifecycleScope.launch {
                val primeNumber = findBigPrime_Wish_To_Be_NonBlocking()
//                val primeNumber = findBigPrime_ProperWay()
                status.text = primeNumber.toString()
                findButton.isEnabled = true
                cancelButton.isEnabled = false
            }
        }

        cancelButton.setOnClickListener {
            findButton.isEnabled = true
            cancelButton.isEnabled = false
            showSnackbar("Cancelling findBigPrime ...")
            status.text = "findBigPrime cancelled"

            primeJob?.cancel()
        }

        countingJob = lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                var value = 0
                while (true) {
                    textView.text = value.toString().also {
                        value++
                    }
                    delay(1_000)
                }
            }
        }
    }

    private fun showSnackbar(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countingJob?.cancel()

        exitProcess(0)
    }

    // Will this help?
    private suspend fun findBigPrime_Wish_To_Be_NonBlocking(): BigInteger =
        BigInteger.probablePrime(2048, Random())

    private suspend fun findBigPrime_ProperWay(): BigInteger = withContext(Dispatchers.Default) {
        BigInteger.probablePrime(2048, Random())
    }
}


