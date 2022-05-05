package com.kkwakjavacoding.kcalendar.weightdatabase

import android.content.Context
import androidx.room.*

@Database(entities = [Weight::class], version = 1, exportSchema = false)
abstract class WeightDatabase : RoomDatabase() {

    abstract fun weightDao(): WeightDao

    companion object {
        private var instance: WeightDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): WeightDatabase {

            if (instance == null) {

                synchronized(WeightDatabase::class) {

                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        WeightDatabase::class.java,
                        "weight_database"
                    ).build()
                }
            }

            return instance!!
        }

    }

}