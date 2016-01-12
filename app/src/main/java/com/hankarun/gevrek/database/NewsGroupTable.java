package com.hankarun.gevrek.database;

import android.database.sqlite.SQLiteDatabase;

public class NewsGroupTable {
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_NEWSGRUOP = "groups";

    public static final String KEY_ID = "_id";
    public static final String NEWSGROUP_NAME = "name";
    public static final String NEWSGROUP_URL = "url";
    public static final String NEWSGROUP_COUNT = "count";
    public static final String NEWSGROUP_COLOR = "color";
    public static final String NEWSGROUP_GROUP = "ggroup";

    public static final String CREATE_TABLE_NEWSGROUPS = "CREATE TABLE "
            + TABLE_NEWSGRUOP + "(" + KEY_ID + " INTEGER PRIMARY KEY," + NEWSGROUP_NAME +" TEXT,"
            + NEWSGROUP_URL + " TEXT,"
            + NEWSGROUP_COUNT + " TEXT,"
            + NEWSGROUP_COLOR + " TEXT,"
            + NEWSGROUP_GROUP + " TEXT"
            +");";

    public static final String[] projection ={
            KEY_ID,
            NEWSGROUP_COUNT,
            NEWSGROUP_GROUP,
            NEWSGROUP_NAME,
            NEWSGROUP_URL,
            NEWSGROUP_COLOR
    };

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_NEWSGROUPS);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWSGRUOP);
        onCreate(database);
    }
}
