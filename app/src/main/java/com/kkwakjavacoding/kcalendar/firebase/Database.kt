package com.kkwakjavacoding.kcalendar.firebase

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
        database.child(id).child(date).child("goal").setValue(nutrition)
    }

    fun insertFood(date: String, time: String, food: Food) {
        database.child(date).child(time).child(food.name)
            .setValue(food) // userToken 밑 child 변경 필요.
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

    suspend fun getFoodName(date: String, time: String): ArrayList<String> {
        val task: Task<DataSnapshot> = database.child(date).child(time).get()
        val deferredDataSnapshot: kotlinx.coroutines.Deferred<DataSnapshot> = task.asDeferred()
        val data: Iterable<DataSnapshot> = deferredDataSnapshot.await().children

        var foods: ArrayList<String> = ArrayList()
        while (data.iterator().hasNext()) {
            foods.add(data.iterator().next().key!!)
        }

        return foods
    }

}
