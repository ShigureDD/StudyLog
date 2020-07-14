package com.example.studylog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class StudyLogSQLite extends SQLiteOpenHelper{
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "StudyLog.db";
    private final  static String TABLE_NAME="studylog";
    private final static String CONTENT="content";
    private final static String TIME="date";
    private final static String ID="_id";
    private static final String Title = "title";
    private static final String PHOTO = "photo";

    private SQLiteDatabase database;


    public StudyLogSQLite(Context context){
        super(context, DATABASE_NAME, null, VERSION);
        database=this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,title TEXT,date TEXT,content TEXT,photo TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
