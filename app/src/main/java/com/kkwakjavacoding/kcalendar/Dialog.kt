package com.kkwakjavacoding.kcalendar

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.kkwakjavacoding.kcalendar.activity.FoodEditActivity
import com.kkwakjavacoding.kcalendar.activity.KcalendarActivity
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import com.kkwakjavacoding.kcalendar.weightdatabase.WeightViewModel
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class Dialog(context: Context) {
    private val dialog = Dialog(context)
    private val context = context
    private lateinit var weight : String

    fun showFoodInfoDialog(date: String, time: String, food: Food) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.custom_food_info_dialog)
        dialog.setCancelable(true) // 바깥 화면 누르면 닫힘.

        dialog.findViewById<ImageView>(R.id.cancelBtn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.modifyButton).setOnClickListener {
            val intent = Intent(this.context, FoodEditActivity::class.java)
            intent.putExtra("date", date)
            intent.putExtra("time", time)
            intent.putExtra("food", food)

            dialog.dismiss()
            context.startActivity(intent)
        }

        dialog.findViewById<TextView>(R.id.foodName).text = food.name
        dialog.findViewById<TextView>(R.id.foodQuantity).text = food.serving.toString() + food.unit
        dialog.findViewById<TextView>(R.id.kcalValue).text = food.kcal.roundToInt().toString()
        dialog.findViewById<TextView>(R.id.carbsValue).text = food.carbs!!.roundToInt().toString()
        dialog.findViewById<TextView>(R.id.proteinValue).text =
            food.protein!!.roundToInt().toString()
        dialog.findViewById<TextView>(R.id.fatValue).text = food.fat!!.roundToInt().toString()
        dialog.findViewById<TextView>(R.id.sugarsValue).text = food.sugars!!.roundToInt().toString()
        dialog.findViewById<TextView>(R.id.sodiumValue).text = food.sodium!!.roundToInt().toString()

        dialog.show()
    }

    fun showWeightInputDialog(flag: Boolean = false) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.weight_input_dialog)
        dialog.setCancelable(false) // 바깥 화면 누르면 닫힘.

        // 처음 설치, 처음 실행
//        if(flag){
//            dialog.findViewById<ImageView>(R.id.)
//        }

        dialog.findViewById<Button>(R.id.weightInputCancelBtn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.weightInputSaveBtn).setOnClickListener {
            weight = dialog.findViewById<EditText>(R.id.weightInput).text.toString()

            onClickedListener.onClicked(weight)

            dialog.dismiss()
        }

        dialog.show()
    }

    interface ButtonClickListener {
        fun onClicked(weight: String)
    }

    private lateinit var onClickedListener: ButtonClickListener

    fun setOnClickedListener(listener: ButtonClickListener) {
        onClickedListener = listener
    }
}