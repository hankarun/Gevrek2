package com.hankarun.gevrek.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(Context context) {
        super(context, NewsGroupTable.TABLE_NEWSGRUOP, null, NewsGroupTable.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        NewsGroupTable.onCreate(db);
        CourseTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        NewsGroupTable.onUpgrade(db,oldVersion,newVersion);
        CourseTable.onUpgrade(db,oldVersion,newVersion);
    }
}
