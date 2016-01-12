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
import android.util.Log;

import com.hankarun.gevrek.database.NewsGroupTable;
import com.hankarun.gevrek.database.NewsgroupDatabaseHelper;


public class NewsContentProvider extends ContentProvider {

    private NewsgroupDatabaseHelper database;

    private static final int NEWSGROUPS = 10;
    private static final int NEWSGROUPID = 20;

    private static final String AUTHORITY = "com.hankarun.gevrek";

    private static final String BASE_PATH = "newsgroups";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/" + BASE_PATH;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, NEWSGROUPS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", NEWSGROUPID);
    }

    @Override
    public boolean onCreate() {
        database = new NewsgroupDatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Set the table
        queryBuilder.setTables(NewsGroupTable.TABLE_NEWSGRUOP);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case NEWSGROUPS:
                break;
            case NEWSGROUPID:
                // adding the ID to the original query
                queryBuilder.appendWhere(NewsGroupTable.KEY_ID + "="
                        + uri.getLastPathSegment());
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
        switch (uriType) {
            case NEWSGROUPS:
                id = sqlDB.insert(NewsGroupTable.TABLE_NEWSGRUOP, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase sqLiteDatabase = database.getWritableDatabase();
        sqLiteDatabase.delete(NewsGroupTable.TABLE_NEWSGRUOP,null,null);
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
