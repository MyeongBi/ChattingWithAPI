package com.example.chattingwithapi.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context):SQLiteOpenHelper(context,"memo.db",null,1,null) {
    override fun onCreate(db: SQLiteDatabase?) {

        val sql=
            "create table note (id integer primary key autoincrement, title text, memo text not null, daytime string)"

        db!!.execSQL(sql)

    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

    }
}