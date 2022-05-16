package com.kkwakjavacoding.kcalendar.activity

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDBHelper(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    var data =  ArrayList<ChartData>()

    companion object{
        val DB_NAME = "weight.db"
        val DB_VERSION = 1
        val TABLE_NAME = "weight"
        val ID = "Id"
        val YEAR = "Year"
        val MONTH = "Month"
        val DAY = "Day"
        val WEIGHT = "Weight"
    }

    fun getData(year:String, month:String) : ArrayList<ChartData>{

        val strsql = "select $DAY, $WEIGHT from $TABLE_NAME where $YEAR='$year' and $MONTH='$month';"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        data.clear()

        if(cursor.count==0){
            cursor.close()
            db.close()
            return data
        }

        do {
            val dayCursor = cursor.getString(0)
            val weightCursor = cursor.getDouble(1)
            data.add(ChartData(dayCursor, weightCursor))
        }while (cursor.moveToNext())

        cursor.close()
        db.close()
        return data

    }

    fun insertData(year: String, month: String, day: String, weight: Double):Boolean {
        val values = ContentValues()
        values.put(YEAR, year)
        values.put(MONTH, month)
        values.put(DAY, day)
        values.put(WEIGHT, weight)
        val db = writableDatabase
        val flag = db.insert(TABLE_NAME, null, values)>0
        db.close()
        return flag
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val create_table = "create table if not exists $TABLE_NAME(" +
                "$ID integer primary key autoincrement, " +  // id값은 입력할 때 자동으로 증가되어 부여됨
                "$YEAR text, " +
                "$MONTH text, " +
                "$DAY text, " +
                "$WEIGHT numeric);"

        db!!.execSQL(create_table)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        val drop_table = "drop table if exists $TABLE_NAME;"
        db!!.execSQL(drop_table)
        onCreate(db)
    }


}