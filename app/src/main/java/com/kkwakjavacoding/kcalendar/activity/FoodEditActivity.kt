package com.kkwakjavacoding.kcalendar.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProvider
import com.kkwakjavacoding.kcalendar.databinding.ActivityFoodEditBinding
import com.kkwakjavacoding.kcalendar.firebase.Database
import com.kkwakjavacoding.kcalendar.firebase.Nutrition
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import com.kkwakjavacoding.kcalendar.fooddatabase.FoodViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round
import kotlin.math.roundToInt

class FoodEditActivity : AppCompatActivity() {
    private val SERVING = "인분"  //this.SERVING
    private val UNIT = "g"
    lateinit var binding: ActivityFoodEditBinding
    private lateinit var foodViewModel: FoodViewModel
    private lateinit var food: Food
    private lateinit var customFood: Food
    private lateinit var beforeEditFood: Food
    private var date = ""
    private var time = ""
    private var quantity: Double = 1.0
    private var serving = SERVING
    private var saltyList = arrayListOf(0.8, 0.9, 1.0, 1.1, 1.2)
    private var sweetList = arrayListOf(0.8, 0.9, 1.0, 1.1, 1.2)
    private var salty = 1.0
    private var sweet = 1.0
    private val servingList = arrayListOf(SERVING, UNIT)
    private val db = Database()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        date = intent.getStringExtra("date")!!
        time = intent.getStringExtra("time")!!
        food = intent.getSerializableExtra("food") as Food
        beforeEditFood = food.copy()

        buttonListener()
        setServingSpinner()
        setServingSpinnerListener()
        setSaltyListener()
        setSweetListener()

        foodViewModel = ViewModelProvider(
            this,
            FoodViewModel.Factory(application)
        )[FoodViewModel::class.java]

        showPredictResult()

    }

    inner class QuantityWatcher : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) { // 숫자가 아닌 경우 예외처리 필요
            if (p0 != null && p0.toString() != "") {
                quantity = p0.toString().toDouble()
                customQuantity()
            }
        }
    }

    private fun setServingSpinner() {
        val unitAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, servingList)
        binding.editServingSpinner.adapter = unitAdapter
    }

    private fun setServingSpinnerListener() {
        binding.editServingSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    serving = binding.editServingSpinner.selectedItem.toString()
                    quantity = binding.customQuantity.text.toString().toDouble()
                    customQuantity()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    private fun showPredictResult() { // 인식 결과를 음식 정보 db에서 찾아 화면에 제공
        foodViewModel.searchName("$food.name").observe(this) {
            customFood = food.copy()
            for (i in it) {
                if (i.brand == food.brand) {
                    food = i.copy()
                    customFood = i.copy()
                    break
                }
            }
            customQuantity()
            binding.customQuantity.addTextChangedListener(QuantityWatcher())
        }
    }

    private fun customQuantity() {
        quantity = binding.customQuantity.text.toString().toDouble()

        if (serving == UNIT) {
            quantity /= food.serving
            quantity = round(quantity * 100) / 100
        }

        customFood = Food(
            0,
            food.name,
            food.classification,
            food.brand,
            round(food.serving.times(quantity) * 100) / 100,
            food.unit,
            round(food.kcal.times(quantity) * 100) / 100,
            round(food.carbs?.times(quantity)?.times(100)!!) / 100,
            round(food.protein?.times(quantity)?.times(100)!!) / 100,
            round(food.fat?.times(quantity)?.times(100)!!) / 100,
            round(food.sugars?.times(quantity)?.times(sweet)?.times(100)!!) / 100,
            round(food.sodium?.times(quantity)?.times(salty)?.times(100)!!) / 100,
        )
        showFoodInfo(customFood)
    }

    private fun showFoodInfo(selectedFood: Food) {
        binding.apply {
            foodName.text = selectedFood.name
            foodServings.text = quantity.toString() + SERVING
            foodQuantity.text = selectedFood.serving.roundToInt().toString() + selectedFood.unit
            foodKcal.text = selectedFood.kcal.roundToInt().toString()
            foodCarbs.text = selectedFood.carbs?.roundToInt().toString()
            foodProtein.text = selectedFood.protein?.roundToInt().toString()
            foodFat.text = selectedFood.fat?.roundToInt().toString()
            foodSugars.text = selectedFood.sugars?.roundToInt().toString()
            foodSodium.text = selectedFood.sodium?.roundToInt().toString()
        }
    }

    private fun setSaltyListener() {
        binding.salty.setOnCheckedChangeListener { _, i ->
            when (i) {
                binding.salty1.id -> salty = saltyList[0]
                binding.salty2.id -> salty = saltyList[1]
                binding.salty3.id -> salty = saltyList[2]
                binding.salty4.id -> salty = saltyList[3]
                binding.salty5.id -> salty = saltyList[4]
            }
            customQuantity()
        }
    }

    private fun setSweetListener() {
        binding.sweet.setOnCheckedChangeListener { _, i ->
            when (i) {
                binding.sweet1.id -> sweet = sweetList[0]
                binding.sweet2.id -> sweet = sweetList[1]
                binding.sweet3.id -> sweet = sweetList[2]
                binding.sweet4.id -> sweet = sweetList[3]
                binding.sweet5.id -> sweet = sweetList[4]
            }
            customQuantity()
        }
    }

    private fun buttonListener() {

        binding.customSaveBtn.setOnClickListener {
            binding.customQuantity.addTextChangedListener(null)
            food = customFood.copy()
            db.insertFood(intent.getStringExtra("date")!!, intent.getStringExtra("time")!!, food)


            MainScope().launch {
                var total: Nutrition
                withContext(Dispatchers.Default) {
                    total = db.getTotal(intent.getStringExtra("date")!!)
                }
                total = subtractTotal(total).copy()
                total = updateTotal(total).copy()
                db.insertTotal(intent.getStringExtra("date")!!, total)
            }
            val intent = Intent(this, KcalendarActivity::class.java)
            startActivity(intent)
        }


        binding.customCancelBtn.setOnClickListener {
            val intent = Intent(this, KcalendarActivity::class.java)
            startActivity(intent)
        }
    }

    private fun subtractTotal(total: Nutrition): Nutrition {
        return Nutrition(
            total.kcal - beforeEditFood.kcal,
            total.carbs - beforeEditFood.carbs!!,
            total.protein - beforeEditFood.protein!!,
            total.fat - beforeEditFood.fat!!,
            total.sugars - beforeEditFood.sugars!!,
            total.sodium - beforeEditFood.sodium!!
        )
    }

    private fun updateTotal(total: Nutrition): Nutrition {
        return Nutrition(
            total.kcal + food.kcal,
            total.carbs + food.carbs!!,
            total.protein + food.protein!!,
            total.fat + food.fat!!,
            total.sugars + food.sugars!!,
            total.sodium + food.sodium!!
        )
    }
}