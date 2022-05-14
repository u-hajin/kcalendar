package com.kkwakjavacoding.kcalendar

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import kotlin.math.roundToInt

class Dialog(context: Context) {
    private val dialog = Dialog(context)

    fun showFoodInfoDialog(food: Food) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_food_info_dialog)
        dialog.setCancelable(true) // 바깥 화면 누르면 닫힘.

        dialog.findViewById<ImageView>(R.id.cancelBtn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.foodName).text = food.name
        dialog.findViewById<TextView>(R.id.foodQuantity).text = food.serving.toString() + food.unit
        dialog.findViewById<TextView>(R.id.kcalValue).text = food.kcal.roundToInt().toString()
        dialog.findViewById<TextView>(R.id.carbsValue).text = food.carbs!!.roundToInt().toString()
        dialog.findViewById<TextView>(R.id.proteinValue).text = food.protein!!.roundToInt().toString()
        dialog.findViewById<TextView>(R.id.fatValue).text = food.fat!!.roundToInt().toString()
        dialog.findViewById<TextView>(R.id.sugarsValue).text = food.sugars!!.roundToInt().toString()
        dialog.findViewById<TextView>(R.id.sodiumValue).text = food.sodium!!.roundToInt().toString()

        dialog.show()
    }
    
    // 나머지 dialog 함수 만들어서 쓰기
}