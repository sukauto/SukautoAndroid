package com.example.newsukauto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.android.synthetic.main.activity_log_reader.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception

class LogReader : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_reader)
        val url = intent.getStringExtra("url")
        val user = intent.getStringExtra("user")
        val password = intent.getStringExtra("password")

        suspend fun loadPage(url: String) {
            try {
                val res = Fuel.get(url).authentication().basic(user, password).awaitString()
                Log.i("Await", "complete")
                textView2.text = res
            } catch (ex: Exception) {
                Log.e("ERROR!", ex.toString())
            }
        }

        runBlocking {
            Log.i("Start load", "")
            val job = GlobalScope.launch { loadPage(url) }
            job.join()
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        finish()
        startActivity(intent)
    }
}
