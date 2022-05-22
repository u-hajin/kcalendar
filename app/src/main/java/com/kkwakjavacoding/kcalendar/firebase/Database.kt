package com.kkwakjavacoding.kcalendar.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import kotlinx.coroutines.tasks.asDeferred
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class Database {

    val id = UserID.getWidevineID()
    val database = Firebase.database.getReference("userToken").child(id)
    var map: HashMap<String, Double> = HashMap()

    fun insertGoal(date: String, nutrition: Nutrition) {
        database.child(date).child("goal").setValue(nutrition)
    }

    fun insertFood(date: String, time: String, food: Food) {
        database.child(date).child(time).child(food.name).setValue(food) // userToken 밑 child 변경 필요.
    }

    fun insertTotal(date: String, nutrition: Nutrition) {
        database.child(date).child("total").setValue(nutrition)
    }

    suspend fun getGoal(date: String): Nutrition {
        var goalMap: HashMap<String, Double> = HashMap()
        val task: Task<DataSnapshot> = database.child(date).get()
        val deferredDataSnapshot: kotlinx.coroutines.Deferred<DataSnapshot> = task.asDeferred()
        val data: Iterable<DataSnapshot> = deferredDataSnapshot.await().children

        while (data.iterator().hasNext()) {
            var snapshot = data.iterator().next()

            if (snapshot.key.toString() == "goal") {
                goalMap = snapshot.value as HashMap<String, Double>
            }
        }
        var goal : Nutrition? = null

        if (goalMap.isNotEmpty()) { // 오늘 목표 이미 있음.
            goal = Nutrition(
                goalMap["kcal"]!!,
                goalMap["carbs"]!!,
                goalMap["protein"]!!,
                goalMap["fat"]!!,
                goalMap["sugars"]!!,
                goalMap["sodium"]!!
            )
        } else if (goalMap.isEmpty() && map.isEmpty()) { // 어제 오늘 다 비어있음
            goal = Nutrition(1900.0, 324.0, 55.0, 54.0, 100.0, 2000.0)
        } else if (goalMap.isEmpty() && map.isNotEmpty()) { // 어제는 있고 오늘은 없음
            goal = Nutrition(
                map["kcal"]!!,
                map["carbs"]!!,
                map["protein"]!!,
                map["fat"]!!,
                map["sugars"]!!,
                map["sodium"]!!
            )
        }

        insertGoal(date, goal!!)
        return goal!!
    }

    suspend fun initGoal(yesterday: String, today: String) {
        var goalMap: HashMap<String, Double> = HashMap()
        val task: Task<DataSnapshot> = database.child(yesterday).get()
        val deferredDataSnapshot: kotlinx.coroutines.Deferred<DataSnapshot> = task.asDeferred()
        val data: Iterable<DataSnapshot> = deferredDataSnapshot.await().children

        while (data.iterator().hasNext()) {
            var snapshot = data.iterator().next()

            if (snapshot.key.toString() == "goal") {
                goalMap = snapshot.value as HashMap<String, Double>
            }
        }

        map = goalMap
    }

    suspend fun getTotal(date: String): Nutrition {
        var totalMap: HashMap<String, Double> = HashMap()
        val task: Task<DataSnapshot> = database.child(date).get()
        val deferredDataSnapshot: kotlinx.coroutines.Deferred<DataSnapshot> = task.asDeferred()
        val data: Iterable<DataSnapshot> = deferredDataSnapshot.await().children

        while (data.iterator().hasNext()) {
            var snapshot = data.iterator().next()

            if (snapshot.key.toString() == "total") {
                totalMap = snapshot.value as HashMap<String, Double>
            }
        }

        var total: Nutrition = if (totalMap.isEmpty()) {
            Nutrition(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
        } else {
            Nutrition(
                totalMap["kcal"]!!,
                totalMap["carbs"]!!,
                totalMap["protein"]!!,
                totalMap["fat"]!!,
                totalMap["sugars"]!!,
                totalMap["sodium"]!!
            )
        }

        return total
    }

    suspend fun getFoodName(date: String, time: String): ArrayList<String> {
        val task: Task<DataSnapshot> = database.child(date).child(time).get()
        val deferredDataSnapshot: kotlinx.coroutines.Deferred<DataSnapshot> = task.asDeferred()
        val data: Iterable<DataSnapshot> = deferredDataSnapshot.await().children

        var foods: ArrayList<String> = ArrayList()
        while (data.iterator().hasNext()) {
            foods.add(data.iterator().next().key!!)
        }

//        if (foods.isEmpty()) {
//            // 음식 없음 예외처리 필요
//        }

        return foods
    }

    suspend fun getFood(date: String, time: String): ArrayList<Food>? {
        var foodMap: HashMap<String, Any>
        var foodList: ArrayList<Food> = ArrayList()

        val task: Task<DataSnapshot> = database.child(date).child(time).get()
        val deferredDataSnapshot: kotlinx.coroutines.Deferred<DataSnapshot> = task.asDeferred()
        val data: Iterable<DataSnapshot> = deferredDataSnapshot.await().children

        while (data.iterator().hasNext()) {
            var snapshot = data.iterator().next()
            foodMap = snapshot.value as HashMap<String, Any>

            var food = Food(
                foodMap["id"].toString().toInt(),
                foodMap["name"].toString(),
                foodMap["brand"].toString(),
                foodMap["classification"].toString(),
                foodMap["serving"].toString().toDouble(),
                foodMap["unit"].toString(),
                foodMap["kcal"].toString().toDouble(),
                foodMap["carbs"].toString().toDouble(),
                foodMap["protein"].toString().toDouble(),
                foodMap["fat"].toString().toDouble(),
                foodMap["sugars"].toString().toDouble(),
                foodMap["sodium"].toString().toDouble()
            )
            foodList.add(food)
        }

        return foodList
    }

    fun deleteFood(date: String, time: String, name: String) {
        database.child(date).child(time).child(name).removeValue()
    }

}


