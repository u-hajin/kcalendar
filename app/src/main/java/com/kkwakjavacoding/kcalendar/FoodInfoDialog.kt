package com.kkwakjavacoding.kcalendar

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import com.kkwakjavacoding.kcalendar.fooddatabase.Food

class FoodInfoDialog(context: Context) {
    private val dialog = Dialog(context)

    fun showDialog(food: Food) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_food_info_dialog)
        dialog.setCancelable(true) // 바깥 화면 누르면 닫힘.

        dialog.findViewById<ImageView>(R.id.cancelBtn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<TextView>(R.id.foodName).text = food.name
        dialog.findViewById<TextView>(R.id.foodQuantity).text = food.serving.toString() + food.unit
        dialog.findViewById<TextView>(R.id.kcalValue).text = food.kcal.toString()
        dialog.findViewById<TextView>(R.id.carbsValue).text = food.carbs.toString()
        dialog.findViewById<TextView>(R.id.proteinValue).text = food.protein.toString()
        dialog.findViewById<TextView>(R.id.fatValue).text = food.fat.toString()
        dialog.findViewById<TextView>(R.id.sugarsValue).text = food.sugars.toString()
        dialog.findViewById<TextView>(R.id.sodiumValue).text = food.sodium.toString()

        dialog.show()
    }
}