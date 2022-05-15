package com.kkwakjavacoding.kcalendar.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kkwakjavacoding.kcalendar.R
import com.kkwakjavacoding.kcalendar.databinding.ActivityCalorieCalculateBinding
import com.kkwakjavacoding.kcalendar.databinding.ActivityFoodEditBinding

class FoodEditActivity : AppCompatActivity() {
    lateinit var binding: ActivityFoodEditBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodEditBinding.inflate(layoutInflater)
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