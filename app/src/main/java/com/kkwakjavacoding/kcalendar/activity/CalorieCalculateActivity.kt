package com.kkwakjavacoding.kcalendar.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kkwakjavacoding.kcalendar.R
import com.kkwakjavacoding.kcalendar.databinding.ActivityCalorieCalculateBinding

class CalorieCalculateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalorieCalculateBinding
    private val activeList = arrayListOf(25, 30, 35, 40)
    private var activeMassValue = activeList[3]
    private var date = ""
    private var month = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalorieCalculateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        date = intent.getStringExtra("date")!!
        month = intent.getIntExtra("month", 0)

        initData()
        radioButtonListener()
        buttonListener()
    }

    private fun buttonListener() {

        binding.customSaveBtn.setOnClickListener {
            val intent = Intent(this, GraphActivity::class.java)
            intent.putExtra("date", date)
            intent.putExtra("month", month)
            savePref()
            startActivity(intent)
        }

        binding.customCancelBtn.setOnClickListener {
            val intent = Intent(this, GraphActivity::class.java)
            intent.putExtra("date", date)
            intent.putExtra("month", month)
            startActivity(intent)
        }
    }


    private fun savePref() {
        val sharedPreferences =
            getSharedPreferences(R.string.sharedPref.toString(), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val height = binding.heightInput.text.toString().toDouble()
        val calorie = (height - 100) * 0.9 * activeMassValue

        editor.putInt(R.string.baseKcal.toString(), calorie.toInt())
        editor.putString(R.string.height.toString(), height.toString())
        editor.putInt(R.string.activeMass.toString(), activeMassValue)
        editor.apply()
    }

    private fun radioButtonListener() {
        binding.activeMass.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.activeMass1 -> activeMassValue = activeList[0]
                R.id.activeMass2 -> activeMassValue = activeList[1]
                R.id.activeMass3 -> activeMassValue = activeList[2]
                R.id.activeMass4 -> activeMassValue = activeList[3]
            }
        }
    }

    private fun initData() {
        val sharedPreferences =
            getSharedPreferences(R.string.sharedPref.toString(), Context.MODE_PRIVATE)

        if (sharedPreferences.contains(R.string.height.toString())) {
            binding.heightInput.setText(sharedPreferences.getString(R.string.height.toString(), ""))
            val sharedActiveMass = sharedPreferences.getInt(R.string.activeMass.toString(), -1)
            activeMassValue = sharedActiveMass
            when (sharedActiveMass) {
                activeList[0] -> {
                    binding.activeMass.check(binding.activeMass1.id)
                }
                activeList[1] -> {
                    binding.activeMass.check(binding.activeMass2.id)
                }
                activeList[2] -> {
                    binding.activeMass.check(binding.activeMass3.id)
                }
                activeList[3] -> {
                    binding.activeMass.check(binding.activeMass4.id)
                }
            }
        }
    }
}