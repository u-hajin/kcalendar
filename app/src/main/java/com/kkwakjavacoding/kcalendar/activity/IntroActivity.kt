package com.kkwakjavacoding.kcalendar.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.kkwakjavacoding.kcalendar.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        Handler(Looper.getMainLooper()).postDelayed({
            var intent = Intent(this, KcalendarActivity::class.java)
            startActivity(intent)
        }, 2000)
    }

    override fun onPause() {
        super.onPause()
        finish()
    }
}