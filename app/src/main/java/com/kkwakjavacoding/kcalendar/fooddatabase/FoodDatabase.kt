package com.kkwakjavacoding.kcalendar.fooddatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Food::class], version = 1, exportSchema = false)
abstract class FoodDatabase : RoomDatabase() {

    abstract fun foodDao(): FoodDao

    companion object {
        private var instance: FoodDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): FoodDatabase {

            if (instance == null) {

                synchronized(FoodDatabase::class) {

                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FoodDatabase::class.java,
                        "food_database"
                    )
                        .createFromAsset("database/food_table.db")
                        .build()
                }
            }

            return instance!!
        }

    }
}