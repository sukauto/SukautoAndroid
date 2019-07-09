package com.example.newsukauto

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity;
import java.lang.Thread.sleep

class Opening : AppCompatActivity() {
    private lateinit var logoAnimation: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.opening)

        val logoAnimate = findViewById<ImageView>(R.id.img).apply {
            setBackgroundResource(R.drawable.animation)
            logoAnimation = background as AnimationDrawable
        }

        logoAnimate.setOnClickListener({
            logoAnimation.start()
            var res = logoAnimation.isRunning
            logoAnimate.setOnClickListener({
                val newIntent = Intent(this, MainActivity::class.java)
                finish()
                startActivity(newIntent)
            })
        })


    }

}
