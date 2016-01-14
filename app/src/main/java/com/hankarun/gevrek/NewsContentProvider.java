package com.hankarun.gevrek;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.hankarun.gevrek.database.CourseTable;
import com.hankarun.gevrek.database.NewsGroupTable;
import com.hankarun.gevrek.database.DatabaseHelper;


public class NewsContentProvider extends ContentProvider {

    private DatabaseHelper database;

    private static final int NEWSGROUPS = 10;
    private static final int NEWSGROUPID = 20;
    private static final int COURSES = 30;
    private static final int COURSEID = 40;

    private static final String AUTHORITY = "com.hankarun.gevrek";

    private static final String BASE_PATH = "newsgroups";
    private static final String BASE_PATH1 = "courses";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final Uri CONTENT_URI1 = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH1);


    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, NEWSGROUPS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", NEWSGROUPID);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH1, COURSES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH1 + "/#", COURSEID);
    }

    @Override
    public boolean onCreate() {
        database = new DatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Set the table

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case NEWSGROUPS:
                queryBuilder.setTables(NewsGroupTable.TABLE_NEWSGRUOP);
                break;
            case NEWSGROUPID:
                // adding the ID to the original query
                queryBuilder.setTables(NewsGroupTable.TABLE_NEWSGRUOP);
                queryBuilder.appendWhere(NewsGroupTable.KEY_ID + "="
                        + uri.getLastPathSegment());
                break;
            case COURSES:
                queryBuilder.setTables(CourseTable.TABLE_COURSES);
                break;
            case COURSEID:
                queryBuilder.setTables(CourseTable.TABLE_COURSES);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        String base = "";
        switch (uriType) {
            case NEWSGROUPS:
                id = sqlDB.insert(NewsGroupTable.TABLE_NEWSGRUOP, null, values);
                base = BASE_PATH;
                break;
            case COURSES:
                id = sqlDB.insert(CourseTable.TABLE_COURSES, null, values);
                base = BASE_PATH1;
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(base + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();;
        switch (uriType){
            case NEWSGROUPS:
                sqLiteDatabase.delete(NewsGroupTable.TABLE_NEWSGRUOP, null, null);
                break;
            case COURSES:
                sqLiteDatabase.delete(CourseTable.TABLE_COURSES, null, null);
                break;
        }

        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case NEWSGROUPS:
                rowsUpdated = sqlDB.update(NewsGroupTable.TABLE_NEWSGRUOP,
                        values,
                        selection,
                        selectionArgs);
                break;
            case NEWSGROUPID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(NewsGroupTable.TABLE_NEWSGRUOP,
                            values,
                            NewsGroupTable.KEY_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(NewsGroupTable.TABLE_NEWSGRUOP,
                            values,
                            NewsGroupTable.KEY_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
