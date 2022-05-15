package com.kkwakjavacoding.kcalendar.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.ScriptGroup
import com.kkwakjavacoding.kcalendar.R
import com.kkwakjavacoding.kcalendar.databinding.ActivityCalorieCalculateBinding

class CalorieCalculateActivity : AppCompatActivity() {
    lateinit var binding: ActivityCalorieCalculateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalorieCalculateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buttonListener()
    }

    private fun buttonListener() {

        binding.customSaveBtn.setOnClickListener{
            val intent = Intent(this, GraphActivity::class.java)
            startActivity(intent)
        }

        binding.customCancelBtn.setOnClickListener{
            val intent = Intent(this, GraphActivity::class.java)
            startActivity(intent)
        }
    }
}