package com.hankarun.gevrek.database;

import android.database.sqlite.SQLiteDatabase;

public class CourseTable {
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_COURSES = "courses";

    public static final String KEY_ID = "_id";
    public static final String COURSE_NAME = "name";
    public static final String COURSE_LONG_NAME = "lname";
    public static final String COURSE_URL = "url";

    public static final String CREATE_TABLE_COURSES = "CREATE TABLE "
            + TABLE_COURSES + "(" + KEY_ID + " INTEGER PRIMARY KEY," + COURSE_NAME +" TEXT,"
            + COURSE_URL + " TEXT,"
            + COURSE_LONG_NAME + " TEXT"
            +");";

    public static final String[] projection ={
            KEY_ID,
            COURSE_NAME,
            COURSE_LONG_NAME,
            COURSE_URL,
    };

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_COURSES);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_COURSES);
        onCreate(database);
    }
}
