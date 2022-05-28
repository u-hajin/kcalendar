package com.kkwakjavacoding.kcalendar

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.kkwakjavacoding.kcalendar.activity.FoodEditActivity
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import com.kkwakjavacoding.kcalendar.weightdatabase.Weight
import com.kkwakjavacoding.kcalendar.weightdatabase.WeightViewModel
import kotlin.math.roundToInt

class Dialog(
    context: Context,
    date: String = "",
    owner: ViewModelStoreOwner? = null,
    application: Application? = null
) {
    private val dialog = Dialog(context)
    private val context = context
    private var today = date
    private val weightViewModel: WeightViewModel = ViewModelProvider(
        owner!!,
        WeightViewModel.Factory(application!!)
    )[WeightViewModel::class.java]

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

        if (flag) {
            dialog.findViewById<Button>(R.id.weightInputCancelBtn).visibility = View.GONE
        } else {
            dialog.findViewById<Button>(R.id.weightInputCancelBtn).visibility = View.VISIBLE
        }

        dialog.findViewById<Button>(R.id.weightInputSaveBtn).setOnClickListener {
            var weightInput = dialog.findViewById<EditText>(R.id.weightInput).text.toString()
            weightViewModel.addWeight(Weight(today, weightInput.toDouble()))
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.weightInputCancelBtn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}