package com.example.newsukauto

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity;
import com.beust.klaxon.Klaxon
import com.example.newsukauto.ui.login.LoginActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.coroutines.awaitString
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    var isLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val env = PreferenceManager.getDefaultSharedPreferences(this)
        val envUrl = env.getString("URL", "")
        val user = env.getString("sukautoUser", "")
        val password = env.getString("sukautoPassword", "")

        data class Service(var name: String, var status: String)
        data class JsonResponse(var services: List<Service>?)

        val linearLayout = findViewById<LinearLayout>(R.id.root_ll)

        fun forceUpdateEnv() {
            env.edit().putInt("dummy", 0).apply()
            env.edit().putInt("dummy", 1).apply()
        }

        //empty auth
        if (user?.trim() == "" || password?.trim() == "") {
            val intent = Intent(this, LoginActivity::class.java)
            isLoad = false
            finish()
            startActivity(intent)
        }

        //TODO set url first and auth second
        //empty URL
        if (envUrl?.trim() == "") {
            val intent = Intent(this, setUrl::class.java)
            isLoad = false
            finish()
            startActivity(intent)
        } else {
            urlLabel.text = envUrl?.trim()
        }

        fun crtText(txt: String) {
            val newLine = TextView(this)
            newLine.textSize = 20f
            newLine.text = txt

            linearLayout.addView(newLine)
        }

        fun fillLayOut(status: String) {
            Log.i("JSONstatus::", status)
            val srvList = Klaxon().parse<JsonResponse>(status)?.services
            if (srvList != null) {
                for (service in srvList) {
                    crtText(service.name + " == " + service.status)
                }
            }
        }

        suspend fun loadPage(url: String) {
            try {
                val res = Fuel.get(url).authentication().basic(user, password).awaitString()
                Log.i("Await", "complete")
                fillLayOut(res)
            } catch (ex: Exception) {
                Log.e("ERROR!", ex.toString())
            }
        }


        if (isLoad) {
            runBlocking {
                val workUrl = urlLabel.text.toString() + "/monitor/status"
                Log.i("Start load", "")
                val job = GlobalScope.launch { loadPage(workUrl) }
                job.join()
            }
        }

        resetBtn.setOnClickListener {
            env.edit().putString("URL", "").apply()
            env.edit().putString("sukautoUser", "").apply()
            env.edit().putString("sukautoPassword", "").apply()

            //TODO restart
            val newIntent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(newIntent)
        }

    }

}
