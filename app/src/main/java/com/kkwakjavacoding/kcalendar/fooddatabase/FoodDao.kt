package com.kkwakjavacoding.kcalendar.fooddatabase

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addFood(food: Food)

    @Insert
    fun addFoodDb(foods: List<Food>)

    @Update
    fun updateFood(food: Food)

    @Delete
    fun deleteFood(food: Food)

    @Query("SELECT * FROM food_table")
    fun readAllData(): Flow<List<Food>>

    @Query("SELECT * FROM food_table WHERE name LIKE :searchQuery")
    fun searchName(searchQuery: String): Flow<List<Food>>

    @Query("SELECT * FROM food_table WHERE classification LIKE :searchQuery")
    fun searchClassification(searchQuery: String): Flow<List<Food>>
}