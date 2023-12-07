package com.kkwakjavacoding.kcalendar.fooddatabase

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
@Entity(tableName = "food_table")
data class Food(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val brand: String,
    val classification: String,
    val serving: Double,
    val unit: String,
    val kcal: Double,
    val carbs: Double?,
    val protein: Double?,
    val fat: Double?,
    val sugars: Double?,
    val sodium: Double?,
) : Serializable