package com.kkwakjavacoding.kcalendar.fooddatabase

import kotlinx.coroutines.flow.Flow

class FoodRepository(private val foodDao: FoodDao) {

    val readAllData: Flow<List<Food>> = foodDao.readAllData()

    suspend fun addFood(food: Food) {
        foodDao.addFood(food)
    }

    suspend fun updateFood(food: Food) {
        foodDao.updateFood(food)
    }

    suspend fun deleteFood(food: Food) {
        foodDao.deleteFood(food)
    }

    fun searchName(searchQuery: String): Flow<List<Food>> {
        return foodDao.searchName(searchQuery)
    }

    fun searchClassification(searchQuery: String): Flow<List<Food>> {
        return foodDao.searchClassification(searchQuery)
    }

    fun searchName(searchQuery: String): Flow<List<Food>> {
        return foodDao.searchName(searchQuery)
    }

    fun searchClassification(searchQuery: String): Flow<List<Food>> {
        return foodDao.searchClassification(searchQuery)
    }
}