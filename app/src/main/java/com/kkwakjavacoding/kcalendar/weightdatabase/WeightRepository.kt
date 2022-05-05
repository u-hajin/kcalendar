package com.kkwakjavacoding.kcalendar.weightdatabase

import kotlinx.coroutines.flow.Flow

class WeightRepository(private val weightDao: WeightDao) {

    val readAllData: Flow<List<Weight>> = weightDao.readAllData()

    suspend fun addWeight(weight: Weight) {
        weightDao.addWeight(weight)
    }

    suspend fun updateWeight(weight: Weight) {
        weightDao.updateWeight(weight)
    }

    suspend fun deleteWeight(weight: Weight) {
        weightDao.deleteWeight(weight)
    }

    fun searchDatabase(searchQuery: String): Flow<List<Weight>> {
        return weightDao.searchDatabase(searchQuery)
    }

    fun getSameMonth(compare: String): Flow<List<Weight>> {
        return weightDao.getSameMonth(compare)
    }

}