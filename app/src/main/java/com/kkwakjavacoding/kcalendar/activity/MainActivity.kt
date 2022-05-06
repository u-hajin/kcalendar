package com.kkwakjavacoding.kcalendar.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import com.kkwakjavacoding.kcalendar.R
import com.kkwakjavacoding.kcalendar.adapter.MyViewPageAdapter
import com.kkwakjavacoding.kcalendar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    val tabTexts = arrayListOf<String>("kcalendar", "graph & goal")
    val tabIcons = arrayListOf<Int>(R.drawable.ic_date_range_fill, R.drawable.ic_graph)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initLayout()
    }

    private fun initLayout() {
        binding.viewPager.adapter = MyViewPageAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setIcon(tabIcons[position])
            tab.text = tabTexts[position]
        }.attach()
    }
}