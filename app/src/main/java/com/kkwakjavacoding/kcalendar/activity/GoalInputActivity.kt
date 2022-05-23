package com.kkwakjavacoding.kcalendar.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kkwakjavacoding.kcalendar.R
import com.kkwakjavacoding.kcalendar.databinding.ActivityGoalInputBinding
import com.kkwakjavacoding.kcalendar.firebase.Database
import com.kkwakjavacoding.kcalendar.firebase.Nutrition

class GoalInputActivity : AppCompatActivity() {
    lateinit var binding: ActivityGoalInputBinding
    private var date = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalInputBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buttonListener()
        date = intent.getStringExtra("date")!!
    }

    private fun buttonListener() {

        binding.customSaveBtn.setOnClickListener{
            val intent = Intent(this, GraphActivity::class.java)
            insertGoal()
            intent.putExtra("date", date)
            startActivity(intent)
        }

        binding.customCancelBtn.setOnClickListener{
            val intent = Intent(this, GraphActivity::class.java)
            intent.putExtra("date", date)
            startActivity(intent)
        }
    }

    private fun insertGoal() {
        // 사용자가 입력한 값들을 변수에 저장
        var kcal = binding.goalKcalInput.text.toString().toDouble()
        var carbohydrate = binding.goalCalboInput.text.toString().toDouble()
        var protein = binding.goalProteinInput.text.toString().toDouble()
        var fat = binding.goalFatInput.text.toString().toDouble()
        var sugar = binding.goalSugarInput.text.toString().toDouble()
        var sodium = binding.goalSodiumInput.text.toString().toDouble()

        // Nutrition 객체 생성 및 사용자 입력 값 저장
        val goal = Nutrition(kcal, carbohydrate, protein, fat, sugar, sodium)

        // firebase 객체 생성 및 DB에 사용자가 입력한 값 저장
        val database = Database()
        database.insertGoal(date, goal)
    }
}