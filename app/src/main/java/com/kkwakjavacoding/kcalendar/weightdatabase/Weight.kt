package com.kkwakjavacoding.kcalendar.weightdatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "weight_table")
data class Weight(
    @PrimaryKey
    val date: String,
    val weight: Double
)
