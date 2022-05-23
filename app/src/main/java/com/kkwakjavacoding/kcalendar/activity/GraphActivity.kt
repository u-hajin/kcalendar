package com.kkwakjavacoding.kcalendar.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.kkwakjavacoding.kcalendar.databinding.ActivityGraphBinding
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.kkwakjavacoding.kcalendar.R
import com.kkwakjavacoding.kcalendar.firebase.Database
import com.kkwakjavacoding.kcalendar.firebase.Nutrition
import com.kkwakjavacoding.kcalendar.weightdatabase.Weight
import com.kkwakjavacoding.kcalendar.weightdatabase.WeightViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.ArrayList

class GraphActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGraphBinding

    private lateinit var weightViewModel: WeightViewModel
    private lateinit var monthAdapter: ArrayAdapter<String>
    private var searchDate: String = ""
    private val monthList = arrayOf(
        "1월", "2월", "3월", "4월", "5월", "6월", "7월",
        "8월", "9월", "10월", "11월", "12월"
    )
    private val dayRegex = "[0-9]{4}-[0-9]+-".toRegex() // yyyy-MM-dd -> dd
    private var chartData = mutableListOf<Entry>()
    private var lineDataSet = ArrayList<ILineDataSet>()
    private var lineData: LineData = LineData()
    private lateinit var chart: LineChart
    private var date = ""
    private var minDay = 1f
    private var maxDay = 1f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        buttonListener()

        chart = binding.weightGraph

        date = intent.getStringExtra("date")!!

        weightViewModel = ViewModelProvider(
            this,
            WeightViewModel.Factory(application)
        )[WeightViewModel::class.java]

        setMonthSpinner()
        setMonthSpinnerListener()

        showBaseCalorie()
        showGoal()

    }

    private fun showBaseCalorie() {
        val sharedPreferences =
            getSharedPreferences(R.string.sharedPref.toString(), Context.MODE_PRIVATE)
        val baseCalorie = sharedPreferences.getInt(R.string.baseKcal.toString(), 0)

        binding.baseKcal.text = " $baseCalorie"
    }

    fun showGoal() {
        val database = Database()
        var date = date
        var goal: Nutrition

        MainScope().launch {
            withContext(Dispatchers.Default) {
                // Firebase 접근
                goal = database.getGoal(date)
            }
            // Firebase에서 읽어온 값을 사용하는 곳
            binding.goalKcal.text = goal.kcal.toInt().toString()
            binding.goalCarbs.text = goal.carbs.toInt().toString()
            binding.carbsPercentage.text =
                "(" + (goal.carbs / 324).times(100).toInt().toString() + "%)"
            binding.goalProtein.text = goal.protein.toInt().toString()
            binding.proteinPercentage.text =
                "(" + (goal.protein / 55).times(100).toInt().toString() + "%)"
            binding.goalFat.text = goal.fat.toInt().toString()
            binding.fatPercentage.text = "(" + (goal.fat / 54).times(100).toInt().toString() + "%)"
            binding.goalSugar.text = goal.sugars.toInt().toString()
            binding.sugarPercentage.text =
                "(" + (goal.sugars / 100).times(100).toInt().toString() + "%)"
            binding.goalSodium.text = goal.sodium.toInt().toString()
            binding.sodiumPercentage.text =
                "(" + (goal.sodium / 2000).times(100).toInt().toString() + "%)"
        }
    }

    private fun buttonListener() {

        binding.calendarImg.setOnClickListener {
            val intent = Intent(this, KcalendarActivity::class.java)
            startActivity(intent)
        }

        binding.calendarText.setOnClickListener {
            val intent = Intent(this, KcalendarActivity::class.java)
            startActivity(intent)
        }

        binding.kcalCalculateBtn.setOnClickListener {
            val intent = Intent(this, CalorieCalculateActivity::class.java)
            startActivity(intent)
        }

        binding.goalInfoBtn.setOnClickListener {
            val intent = Intent(this, GoalInputActivity::class.java)
            intent.putExtra("date", date)
            startActivity(intent)
        }
    }

    private fun getWeight() {

        weightViewModel.searchDatabase("$searchDate%").observe(this) {
            for (weight in it) {
                chartData.add(
                    Entry(
                        weight.date.replace(dayRegex, "").toFloat(),
                        weight.weight.toFloat()
                    )
                )
            }

            if (it.isNotEmpty()) {
                minDay = it[0].date.replace(dayRegex, "").toFloat()
                maxDay = it[it.size - 1].date.replace(dayRegex, "").toFloat()
                drawGraph()
            } else {
                chart.clear()
            }
        }
    }

    private fun setMonthSpinner() {
        monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, monthList)
        binding.monthSpinner.adapter = monthAdapter
    }

    private fun setMonthSpinnerListener() {
        binding.monthSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    var year = Calendar.getInstance().get(Calendar.YEAR).toString()
                    var selected = binding.monthSpinner.selectedItem.toString()
                    var month =
                        selected.substring(0 until selected.length - 1) // 숫자만 남기기

                    if (month.length == 1) {
                        month = "0$month"
                    }
                    searchDate = "$year-$month"

                    getWeight()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    private fun drawGraph() {
        val set = LineDataSet(chartData, "weight")

        // data 적용
        lineDataSet.add(set)

        lineData = LineData(lineDataSet)
//        lineData.setValueTextSize(12f)
//        lineData.setValueTextColor(R.color.black)


        set.lineWidth = 2f
        set.setDrawValues(false)
        set.color = ContextCompat.getColor(application, R.color.deep_green)
//        set.highLightColor = R.color.deep_green
        set.setDrawCircles(true)
        set.setDrawCircleHole(true)
        set.setCircleColor(R.color.black)
        set.circleHoleColor = ContextCompat.getColor(application, R.color.black)
        set.circleHoleRadius = 7f
        set.mode = LineDataSet.Mode.LINEAR

        // 클릭하면 highlight에서 체중 보여주기

        // x축 설정
        val xAxis = chart.xAxis
        xAxis.setDrawLabels(true)
        xAxis.axisMaximum = maxDay
        xAxis.axisMinimum = minDay
        xAxis.labelCount = (maxDay - minDay).toInt()
        if (xAxis.labelCount > 10) {
            xAxis.labelCount = 10
        }
        xAxis.setDrawGridLines(false)

        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawLabels(true)
        xAxis.setDrawAxisLine(true)

        chart.setNoDataText("해당 월의 체중 데이터가 없습니다.")
        chart.setNoDataTextColor(R.color.deep_green)

        chart.setDrawGridBackground(false)
        chart.legend.isEnabled = false

        // 오른쪽 y축 안 보이게
        chart.axisRight.isEnabled = false

        chart.description.isEnabled = false
        chart.data = lineData
        chart.invalidate()

    }

}