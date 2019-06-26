package com.example.newsukauto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText as EditTxt

class setUrl : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_url)
        val env = PreferenceManager.getDefaultSharedPreferences(this)

        val urlLine = findViewById<EditTxt>(R.id.url)
        val okBtn = findViewById<Button>(R.id.okBtn)

        okBtn.setOnClickListener {
            env.edit().putString("URL", urlLine.getText().toString()).apply()
            val intent = Intent(this, MainActivity::class.java)
            finish()
            startActivity(intent)
        }
    }
}
