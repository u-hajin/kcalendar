package com.kkwakjavacoding.kcalendar.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.kkwakjavacoding.kcalendar.databinding.ActivityFoodCustomBinding
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import com.kkwakjavacoding.kcalendar.fooddatabase.FoodViewModel
import com.kkwakjavacoding.kcalendar.weightdatabase.WeightViewModel
import kotlin.math.roundToInt

const val IMAGE_MEAN = 127.5f
const val IMAGE_STD = 127.5f

class FoodCustomActivity : AppCompatActivity() {

    lateinit var binding: ActivityFoodCustomBinding
    lateinit var foodViewModel: FoodViewModel
    lateinit var weightViewModel: WeightViewModel
    lateinit var brandList: ArrayList<String>
    lateinit var foodNameList: ArrayList<String>
    lateinit var checkBoxList: ArrayList<CheckBox>
    var brand: String = ""
    lateinit var predictResult: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        predictResult = intent.getStringExtra("result")!!
        getCheckBox()

        foodViewModel = ViewModelProvider(
            this,
            FoodViewModel.Factory(application)
        )[FoodViewModel::class.java]

        weightViewModel = ViewModelProvider(
            this,
            WeightViewModel.Factory(application)
        )[WeightViewModel::class.java]

        getFoodInfo()
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
            binding.dairyCheckBox,
            binding.drinkCheckBox,
            binding.pancakeCheckBox,
            binding.boiledCheckBox,
            binding.stewCheckBox,
            binding.steamedCheckBox,
            binding.vegetableCheckBox,
            binding.chickenCheckBox,
            binding.friedCheckBox,
            binding.pastaCheckBox,
            binding.pizzaCheckBox,
        )
    }

    private fun findFirstFood(it: List<Food>) { // 제일 맨 위 음식 가져오기
        binding.foodName.text = it[0].name
        binding.foodQuantity.text = it[0].serving.roundToInt().toString() + it[0].unit
        binding.foodKcal.text = it[0].kcal.toString()
        binding.foodCarbs.text = it[0].carbs.toString()
        binding.foodProtein.text = it[0].protein.toString()
        binding.foodFat.text = it[0].fat.toString()
        binding.foodSugars.text = it[0].sugars.toString()
        binding.foodSodium.text = it[0].sodium.toString()
    }

    private fun setCheckBox(it: List<Food>) { // spinner 채우기
        var classification = it[0].classification

        for (i in checkBoxList) {
            if (classification == i.text.toString()) {
                i.isChecked = true
            }
        }
    }

    private fun setBrandSpinner(it: List<Food>) { // spinner 채우기
        brandList = ArrayList()

        for (i in it) {
            if (i.classification == it[0].classification && !brandList.contains(i.brand)) {
                brandList.add(i.brand)
            }
        }

        val adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, brandList)

        binding.brandSpinner.adapter = adapter

        binding.brandSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                brand = binding.brandSpinner.selectedItem.toString()
                getBrandFoodName(it)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun setFoodNameSpinner() {

    }

    private fun getFoodInfo() {
        var searchQuery = "%$predictResult%"

        foodViewModel.searchName(searchQuery).observe(this) {

            if (it.isEmpty()) {
                Toast.makeText(this, "일치하는 이름이 없습니다.", Toast.LENGTH_LONG).show()
            } else {
                findFirstFood(it)
                setCheckBox(it)
                setBrandSpinner(it)
            }

        }

    }

    private fun updateSpinner() {

        for (i in checkBoxList) {
            if (i.isChecked) {

            }
        }

        // 제품명 업데이트도 같이 해야됨.
    }

    private fun getBrandFoodName(it: List<Food>) {
        foodNameList = ArrayList()

        for (i in it) {

            if (i.brand == this.brand) {
                foodNameList.add(i.name)
            }

        }
        val adapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, foodNameList)

        binding.foodNameSpinner.adapter = adapter

        binding.foodNameSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }


}