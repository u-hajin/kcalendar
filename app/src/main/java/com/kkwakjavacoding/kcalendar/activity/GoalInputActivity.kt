package com.kkwakjavacoding.kcalendar.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kkwakjavacoding.kcalendar.R
import com.kkwakjavacoding.kcalendar.databinding.ActivityGoalInputBinding

class GoalInputActivity : AppCompatActivity() {
    lateinit var binding: ActivityGoalInputBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalInputBinding.inflate(layoutInflater)
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