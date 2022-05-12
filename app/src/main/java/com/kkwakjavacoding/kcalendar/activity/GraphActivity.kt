package com.kkwakjavacoding.kcalendar.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kkwakjavacoding.kcalendar.databinding.ActivityGraphBinding

class GraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGraphBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}