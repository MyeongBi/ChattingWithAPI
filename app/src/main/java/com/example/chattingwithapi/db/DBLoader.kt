package com.example.chattingwithapi.db

import android.content.ContentValues
import android.content.Context
import android.widget.Toast
import com.example.chattingwithapi.model.Memo
import java.util.Calendar

class DBLoader(context: Context) {

    private val context=context
    private var db: DBHelper

    init {
        db= DBHelper(context);
    }

    fun save(title:String, memo:String,getCalendar: Calendar?){
        var calendar:Calendar
        if(getCalendar==null){
            calendar=Calendar.getInstance()
        }else{
            calendar=getCalendar
        }
        val contentValues=ContentValues()
        contentValues.put("title",title)
        contentValues.put("memo",memo)
        contentValues.put("daytime",calendar.timeInMillis)
        db.writableDatabase.insert("note",null,contentValues)
        db.close()
        Toast.makeText(context,"저장됨",Toast.LENGTH_SHORT).show()

    }

    fun delete(id:Int){
        db.writableDatabase.delete("note","id=?", arrayOf(id.toString()))
        db.close()
        Toast.makeText(context,"삭제됨",Toast.LENGTH_SHORT).show()

    }

    fun update(id:Int,title: String,memo: String){
        val contentValues=ContentValues()
        contentValues.put("title",title)
        contentValues.put("memo",memo)
        db.writableDatabase.update("note",contentValues,"id=?", arrayOf(id.toString()))
        db.close()
    }

    fun memoList(datetime:Long?):ArrayList<Memo>{
        val array=ArrayList<Memo>()
        var sql=""
        if(datetime==null){
            sql="select *from note order by daytime desc"
        }else{
            sql="select *from note where daytime like '%" +datetime +"%' order by daytime desc"
        }
        val cursor=db.readableDatabase.rawQuery(sql,null)
        while (cursor.moveToNext()){
            val id=cursor.getInt(cursor.getColumnIndexOrThrow("id"))
            val title=cursor.getString(cursor.getColumnIndexOrThrow("title"))
            val memo=cursor.getString(cursor.getColumnIndexOrThrow("memo"))
            val getDatetime=cursor.getLong(cursor.getColumnIndexOrThrow("daytime"))

            val memoItem= Memo(id,title,memo,getDatetime)
            array.add(memoItem)
        }
        return array


    }

}