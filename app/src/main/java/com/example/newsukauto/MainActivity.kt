package com.example.newsukauto

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import com.example.newsukauto.ui.login.LoginActivity

import kotlinx.coroutines.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_set_url.*
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)
        val env = PreferenceManager.getDefaultSharedPreferences(this)
        val envUrl = env.getString("URL", "")
        val user = env.getString("sukautoUser", "")
        val password = env.getString("sukautoPassword", "")

        fun forceUpdateEnv() {
            env.edit().putInt("dummy", 0).apply()
            env.edit().putInt("dummy", 1).apply()
        }

        //empty auth
        if (user?.trim() == "" || password?.trim() == "") {
            val intent = Intent(this, LoginActivity::class.java)
            finish()
            startActivity(intent)
        }

        //TODO set url first and auth second
        //empty URL
        if (envUrl?.trim() == "") {
            val intent = Intent(this, setUrl::class.java)
            finish()
            startActivity(intent)
        } else {
            urlLabel.text = envUrl?.trim()
        }


        GlobalScope.launch { // launch a new coroutine in background and continue
            delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
//            val res = URL(urlLabel.text.toString() + "/monitor/status").readText()
            val res = URL("https://www.google.com/").readText()
            println("dbg_start!") // print after delay
            println(res)
            println("dbg_end")
        }
//
//        suspend fun readUrl() = coroutineScope {
//            val task1 = async { fetchResult(id = 42) }
//            val task2 = async { fetchResult(id = 99) }
//
//            val results = awaitAll(task1, task2)
//
//            log(results)
//        }


//        suspend fun readUrl(urlAdress: String) {
////            val res =  async { URL(urlLabel.text.toString() + "/monitor/status").readText() }.await
//            try {
//                var res = ""
//                res = async {  URL(urlAdress).readText() }.await
//                Log.i("[RES]", res.toString())
//            } catch (ex: Exception) {
//
//            }
//
//        }
//
//        readUrl(urlLabel.text.toString() + "/monitor/status")

        resetBtn.setOnClickListener {
            env.edit().putString("URL", "").apply()
            env.edit().putString("sukautoUser", "").apply()
            env.edit().putString("sukautoPassword", "").apply()

            //TODO restart
            val newIntent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(newIntent)
        }

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null).show()
//        }
    }

}
