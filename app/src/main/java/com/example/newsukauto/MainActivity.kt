package com.example.newsukauto

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import com.beust.klaxon.Klaxon
import com.example.newsukauto.ui.login.LoginActivity
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_main.*
import com.github.kittinunf.result.Result

class MainActivity : AppCompatActivity() {
    var isLoad = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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

        data class Service(var name:String, var status: String)
        data class JsonResponse(var sevices: List<Service>)
        fun fillLayOut(status: String) {
            Log.i("DEBUGstatus::",status)
            val res = Klaxon().parse<JsonResponse>(status)
            if (res != null) {
                for (rs in res.sevices) {
                      Log.i("DEBUG::", rs.toString())
                }
            }
        }

        fun loadPage(url: String) {
            val asyncRq = url
                .httpGet()
                .authentication()
                .basic(user, password)
                .responseString { request, response, result ->
                    when (result) {
                        is Result.Failure -> {
                            val ex = result.getException()
                            Log.e("LOAD_ERROR", ex.toString())
                        }
                        is Result.Success -> {
                            val data = result.get()
                            Log.i("data_info", data)
//                            fillLayOut(data)
                        }
                    }
                }
            asyncRq.join()
        }

        if (isLoad) {
            val workUrl = urlLabel.text.toString() + "/monitor/status"
            loadPage(workUrl)
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
