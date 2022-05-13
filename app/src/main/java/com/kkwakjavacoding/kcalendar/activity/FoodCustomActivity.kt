package com.kkwakjavacoding.kcalendar.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.kkwakjavacoding.kcalendar.databinding.ActivityFoodCustomBinding
import com.kkwakjavacoding.kcalendar.firebase.Database
import com.kkwakjavacoding.kcalendar.firebase.Nutrition
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import com.kkwakjavacoding.kcalendar.fooddatabase.FoodViewModel
import com.kkwakjavacoding.kcalendar.weightdatabase.WeightViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round
import kotlin.math.roundToInt

const val SERVING = "인분"
const val UNIT = "g"

class FoodCustomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodCustomBinding
    private lateinit var predictResult: String
    private lateinit var foodViewModel: FoodViewModel
    private lateinit var weightViewModel: WeightViewModel
    private lateinit var checkBoxList: ArrayList<CheckBox>
    private lateinit var food: Food
    private lateinit var customFood: Food
    private var brand: String = ""
    private var brandList: ArrayList<String> = ArrayList()
    private var foodNameList: ArrayList<String> = ArrayList()
    private val servingList = arrayListOf(SERVING, UNIT)
    private lateinit var brandAdapter: ArrayAdapter<String>
    private lateinit var foodNameAdapter: ArrayAdapter<String>
    private var selected: String = ""
    private var classList: ArrayList<String> = ArrayList()
    private var searchList: List<Food> = listOf()
    private var serving = SERVING
    private var quantity: Double = 1.0
    private var saltyList = arrayListOf(0.8, 0.9, 1.0, 1.1, 1.2)
    private var sweetList = arrayListOf(0.8, 0.9, 1.0, 1.1, 1.2)
    private var salty = 1.0
    private var sweet = 1.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getCheckBox()

        // date, time도 intent로 받아야 함.
        predictResult = intent.getStringExtra("result")!!

        setServingSpinner()
        setServingSpinnerListener()
        setBrandSpinnerListener()
        setFoodNameSpinnerListener()
        setSaltyListener()
        setSweetListener()
        buttonListener()

        foodViewModel = ViewModelProvider(
            this,
            FoodViewModel.Factory(application)
        )[FoodViewModel::class.java]

        weightViewModel = ViewModelProvider(
            this,
            WeightViewModel.Factory(application)
        )[WeightViewModel::class.java]

        foodNameList.add("")
        foodNameAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, foodNameList)
        binding.foodNameSpinner.adapter = foodNameAdapter

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

    private fun getCheckBox() {
        checkBoxList = arrayListOf(
            binding.roastCheckBox,
            binding.soupCheckBox,
            binding.kimchiCheckBox,
            binding.dessertCheckBox,
            binding.ricecakeCheckBox,
            binding.noddleCheckBox,
            binding.riceCheckBox,
            binding.burgerCheckBox,
            binding.stirCheckBox,
            binding.breadCheckBox,
            binding.sandwichCheckBox,
            binding.drinkCheckBox,
            binding.pancakeCheckBox,
            binding.boiledCheckBox,
            binding.steamedCheckBox,
            binding.vegetableCheckBox,
            binding.chickenCheckBox,
            binding.friedCheckBox,
            binding.pastaCheckBox,
            binding.pizzaCheckBox
        )
    }

    private fun showPredictResult() { // 인식 결과를 음식 정보 db에서 찾아 화면에 제공
        foodViewModel.searchName("%$predictResult%").observe(this) {
            if (it.isEmpty()) {
                Toast.makeText(this, "일치하는 음식이 없습니다.", Toast.LENGTH_LONG).show()
            } else {
                food = it[0].copy()
                customFood = it[0].copy()
                showFoodInfo(this.food)
                setCheckBoxAuto()
                setBrandSpinnerFirst(it)
                binding.customQuantity.addTextChangedListener(QuantityWatcher())
            }
        }
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

    private fun setCheckBoxAuto() { // 인식하고 체크박스 자동 체크
        brand = food.brand

        for (i in checkBoxList) {
            if (food.classification == i.text.toString()) {
                classList.add(food.classification)
                i.isChecked = true
                break
            }
        }
    }

    // 이름에 predictResult가 들어가고 상세분류가 같은 음식들의 브랜드로 spinner 채우기
    private fun setBrandSpinnerFirst(it: List<Food>) {
        for (i in it) {
            if (i.classification == food.classification && !brandList.contains(i.brand)) {
                brandList.add(i.brand)
            }
        }

        brandAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, brandList)
        binding.brandSpinner.adapter = brandAdapter
        setBrandSpinnerListener()
    }

    private fun setBrandSpinnerListener() {
        binding.brandSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    brand = binding.brandSpinner.selectedItem.toString()
                    setFoodNameSpinner()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    private fun setFoodNameSpinnerListener() {
        binding.foodNameSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    selected = binding.foodNameSpinner.selectedItem.toString()
                    quantity = 1.0
                    serving = SERVING
                    binding.customQuantity.setText("1.0")
                    binding.servingSpinner.setSelection(0)
                    matchSelectedFood()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    fun updateBrandSpinner(view: View) {
        brandList.clear()
        classList.clear()

        for (i in checkBoxList) {
            if (i.isChecked) {
                classList.add(i.text.toString())
                foodViewModel.searchClassification(i.text.toString()).observe(this) {
                    for (j in it) {
                        if (!brandList.contains(j.brand)) {
                            brandList.add(j.brand)
                        }
                    }
                }
            }
        }
    }

    private fun setFoodNameSpinner() {
        foodNameList.clear()

        foodViewModel.searchBrand(brand).observe(this) {
            for (i in it) {
                if (classList.contains(i.classification)) {
                    foodNameList.add(i.name)
                }
            }
            searchList = it
        }

    }

    private fun matchSelectedFood() {
        for (i in searchList) {
            if (i.name == selected) {
                food = i.copy()
                customFood = i.copy()
                break
            }
        }
        showFoodInfo(food)
    }

    private fun buttonListener() {
        binding.customSaveBtn.setOnClickListener {
            binding.customQuantity.addTextChangedListener(null)
            food = customFood.copy()
            val db = Database()
            db.insertFood(intent.getStringExtra("date")!!, intent.getStringExtra("time")!!, food)

            MainScope().launch {
                var total: Nutrition
                withContext(Dispatchers.Default) {
                    total = db.getTotal(intent.getStringExtra("date")!!)
                }
                total = updateTotal(total).copy()
                db.insertTotal(intent.getStringExtra("date")!!, total)
            }
            val intent = Intent(this, KcalendarActivity::class.java)
            startActivity(intent)
        }

        binding.customCancelBtn.setOnClickListener {
            finish()
        }
    }

    private fun setServingSpinner() {
        val unitAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, servingList)
        binding.servingSpinner.adapter = unitAdapter
    }

    private fun setServingSpinnerListener() {
        binding.servingSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    serving = binding.servingSpinner.selectedItem.toString()
                    quantity = binding.customQuantity.text.toString().toDouble()
                    customQuantity()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    private fun customQuantity() {
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

}