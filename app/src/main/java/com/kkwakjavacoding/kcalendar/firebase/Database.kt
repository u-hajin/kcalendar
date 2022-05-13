package com.kkwakjavacoding.kcalendar.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.kkwakjavacoding.kcalendar.fooddatabase.Food
import kotlinx.coroutines.tasks.asDeferred

class Database {

    val id = UserID.getWidevineID()
    val database = Firebase.database.getReference("userToken").child(id)

    fun insertGoal(date: String, nutrition: Nutrition) {
        database.child(date).child("goal").setValue(nutrition)
    }

    fun insertFood(date: String, time: String, food: Food) {
        database.child(date).child(time).child(food.name).setValue(food) // userToken 밑 child 변경 필요.
    }

    fun insertTotal(date: String, nutrition: Nutrition) {
        database.child(date).child("total").setValue(nutrition)
    }

    suspend fun getGoal(date: String): HashMap<String, Any> {
        var goal: HashMap<String, Any> = HashMap()
        val task: Task<DataSnapshot> = database.child(date).get()
        val deferredDataSnapshot: kotlinx.coroutines.Deferred<DataSnapshot> = task.asDeferred()
        val data: Iterable<DataSnapshot> = deferredDataSnapshot.await().children

        while (data.iterator().hasNext()) {
            var snapshot = data.iterator().next()

            if (snapshot.key.toString() == "goal") {
                goal = snapshot.value as HashMap<String, Any>
            }
        }

        return goal
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


