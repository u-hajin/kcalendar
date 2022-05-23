package com.kkwakjavacoding.kcalendar.weightdatabase

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addWeight(weight: Weight)

    @Insert
    fun addWeightDb(weights: List<Weight>)

    @Update
    fun updateWeight(weight: Weight)

    @Delete
    fun deleteWeight(weight: Weight)

    @Query("SELECT * FROM weight_table")
    fun readAllData(): Flow<List<Weight>>

    @Query("SELECT * FROM weight_table WHERE date LIKE :searchQuery ORDER BY date ASC")
    fun searchDatabase(searchQuery: String): Flow<List<Weight>>

    @Query("SELECT * FROM weight_table WHERE date == :date")
    fun searchDate(date: String): Flow<List<Weight>>

}