package com.kkwakjavacoding.kcalendar.weightdatabase

import kotlinx.coroutines.flow.Flow

class WeightRepository(private val weightDao: WeightDao) {

    val readAllData: Flow<List<Weight>> = weightDao.readAllData()

    fun addWeight(weight: Weight) {
        weightDao.addWeight(weight)
    }

    fun updateWeight(weight: Weight) {
        weightDao.updateWeight(weight)
    }

    fun deleteWeight(weight: Weight) {
        weightDao.deleteWeight(weight)
    }

    fun searchDatabase(searchQuery: String): Flow<List<Weight>> {
        return weightDao.searchDatabase(searchQuery)
    }

    fun searchDate(date: String): Flow<List<Weight>> {
        return weightDao.searchDate(date)
    }

}