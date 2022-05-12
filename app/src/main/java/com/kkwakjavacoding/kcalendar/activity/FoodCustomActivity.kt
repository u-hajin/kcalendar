package com.kkwakjavacoding.kcalendar.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.kkwakjavacoding.kcalendar.databinding.ActivityFoodCustomBinding
import com.kkwakjavacoding.kcalendar.firebase.Database
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import com.kkwakjavacoding.kcalendar.fooddatabase.FoodViewModel
import com.kkwakjavacoding.kcalendar.weightdatabase.WeightViewModel
import kotlin.math.roundToInt

class FoodCustomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodCustomBinding
    private lateinit var predictResult: String
    private lateinit var foodViewModel: FoodViewModel
    private lateinit var weightViewModel: WeightViewModel
    private lateinit var checkBoxList: ArrayList<CheckBox>
    private lateinit var food: Food
    private var brand: String = ""
    private var brandList: ArrayList<String> = ArrayList()
    private var foodNameList: ArrayList<String> = ArrayList()
    private lateinit var brandAdapter: ArrayAdapter<String>
    private lateinit var foodNameAdapter: ArrayAdapter<String>
    private var selected: String = ""
    private var classList: ArrayList<String> = ArrayList()
    private var searchList: List<Food> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getCheckBox()

        // date, time도 intent로 받아야 함.
        predictResult = intent.getStringExtra("result")!!

        setBrandSpinnerListener()
        setFoodNameSpinnerListener()
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

    private fun getCheckBox() {
        checkBoxList = arrayListOf<CheckBox>(
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
            binding.pizzaCheckBox,
        )
    }

    private fun showPredictResult() { // 인식 결과를 음식 정보 db에서 찾아 화면에 제공
        foodViewModel.searchName("%$predictResult%").observe(this) {
            if (it.isEmpty()) {
                Toast.makeText(this, "일치하는 음식이 없습니다.", Toast.LENGTH_LONG).show()
            } else {
                food = it[0].copy()
                showFoodInfo()
                setCheckBoxAuto()
                setBrandSpinnerFirst(it)
            }
        }
    }

    private fun showFoodInfo() {
        binding.foodName.text = food.name
        binding.foodQuantity.text = food.serving.roundToInt().toString() + food.unit
        binding.foodKcal.text = food.kcal.toString()
        binding.foodCarbs.text = food.carbs.toString()
        binding.foodProtein.text = food.protein.toString()
        binding.foodFat.text = food.fat.toString()
        binding.foodSugars.text = food.sugars.toString()
        binding.foodSodium.text = food.sodium.toString()
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
//        foodViewModel.searchBrand(brand).observe(this) {
//            for (i in it) {
//                if (i.name == selected) {
//                    food = i
//                    break
//                }
//            }
//            showFoodInfo()
//        }
        for (i in searchList) {
            if (i.name == selected) {
                food = i
                break
            }
        }
        showFoodInfo()
    }

    private fun buttonListener() {
        binding.customSaveBtn.setOnClickListener {
            val db = Database()
//            db.insertFood(intent.getStringExtra("date")!!, intent.getStringExtra("time")!!, food)
            // 다시 kcalendarActivity로 화면 전환
        }

        binding.customCancelBtn.setOnClickListener {
            finish()
        }
    }

}