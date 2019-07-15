package com.example.newsukauto

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity;
import com.beust.klaxon.Klaxon
import com.example.newsukauto.ui.login.LoginActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.authentication
import com.github.kittinunf.fuel.coroutines.awaitString
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    var isLoad = true
    var url = ""

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

        val imgSukauto = ImageView(this)
        val imgId = getResources().getDrawable(R.drawable.ico)
        imgSukauto.setImageDrawable(imgId)
//        val lst = arrayOf(imgSukauto, "restart", "update", "stop")
        val lst = arrayOf("...", "restart", "update", "stop")

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
            url = "${urlLabel?.text}/monitor"
        }

        fun generateHollowBtn(ctx: Context, name: String): MaterialButton {
            val btn = MaterialButton(ctx)
            val blue = getResources().getColor(R.color.white)
            val hollow = getResources().getColor(R.color.hollow)
            val violet = getResources().getColor(R.color.dark_pointer)

            btn.text = name
            btn.cornerRadius = R.string.bs
            btn.highlightColor = blue
            btn.setTextColor(blue)
            btn.setLinkTextColor(violet)
            btn.setRippleColorResource(R.color.light_blue)
            btn.setBackgroundColor(hollow)

            return btn
        }

        fun generateStatusText(status: String): TextView {
            val text = TextView(this)
            text.textSize = 15f
            text.text = status
            text.gravity = Gravity.CENTER
            if (status == "running") {
                val green = getResources().getColor(R.color.green)
                text.setTextColor(green)
            }
            return text
        }

//        fun generateIcon(): ImageView {
//            val img = ImageView(this)
//            val ico = getResources().getDrawable(R.drawable.ico)
//            img.setImageDrawable(ico)
//            return img
//        }

        fun generateIcon(): Spinner {
            val spinner = Spinner(this)
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, lst)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
            return spinner
        }

        suspend fun sendRq(url: String) {
            try {
                val res = Fuel.get(url).authentication().basic(user, password).awaitString()
                Log.i("Await", "complete")
            } catch (ex: Exception) {
                Log.e("ERROR!", ex.toString())
            }
        }

        fun crtLine(name: String, status: String) {
            val line = LinearLayout(this)

            val srv = generateHollowBtn(this, name)
            val cfg = generateIcon()
            val text = generateStatusText(status)

            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
            val params = LinearLayout.LayoutParams(width, height)

            srv.setLayoutParams(TableRow.LayoutParams(width, height, 3f))
            text.setLayoutParams(TableRow.LayoutParams(width, height, 3f))
            cfg.setLayoutParams(TableRow.LayoutParams(width, height, 4f))

            line.addView(srv)
            line.addView(text)
            line.addView(cfg)

            linearLayout.addView(line, params)

            // ---------- actions ----------
            srv.setOnClickListener() {
                val targetUrl = "${url}/log/$name"
                Log.i("RUN", targetUrl)
                val newIntent = Intent(this, LogReader::class.java)
                newIntent.putExtra("url", targetUrl)
                newIntent.putExtra("password", password)
                newIntent.putExtra("user", user)
                finish()
                startActivity(newIntent)
            }

            cfg.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    var workUrl = ""
                    if (position == 1) {
                        workUrl = "${url}/restart/$name"
                    }

                    if (position == 2) {
                        workUrl = "${url}/update/$name"
                    }

                    if (position == 3) {
                        workUrl = "${url}/stop/$name"
                    }
                    if (workUrl != "") {
                        Log.i("item_select", workUrl)
                        runBlocking {
                            Log.i("Start load", "")
                            val job = GlobalScope.launch { sendRq(workUrl) }
                            job.join()
                        }
                        finish()
                        startActivity(getIntent())
                    }
                }
            }

        }

        fun fillLayOut(status: String) {
            Log.i("JSONstatus::", status)
            val srvList = Klaxon().parse<JsonResponse>(status)?.services
            if (srvList != null) {
                for (service in srvList) {
                    crtLine(service.name, service.status)
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
                val workUrl = url + "/status"
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
