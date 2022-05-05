package com.kkwakjavacoding.kcalendar.weightdatabase

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Insert
    fun addWeight(weight: Weight)

    @Insert
    fun addWeightDb(weights: List<Weight>)

    @Update
    fun updateWeight(weight: Weight)

    @Delete
    fun deleteWeight(weight: Weight)

    @Query("SELECT * FROM weight_table")
    fun readAllData(): Flow<List<Weight>>

    @Query("SELECT * FROM weight_table WHERE weight LIKE :searchQuery")
    fun searchDatabase(searchQuery: String): Flow<List<Weight>>

    // 조건 수정 필요
    @Query("SELECT * FROM weight_table WHERE date < :compare")
    fun getSameMonth(compare: String): Flow<List<Weight>>
}